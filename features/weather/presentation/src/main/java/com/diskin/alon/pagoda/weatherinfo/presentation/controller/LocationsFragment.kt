package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.FragmentLocationsBinding
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.LocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@OptionalInject
@AndroidEntryPoint
class LocationsFragment : Fragment(), SearchView.OnQueryTextListener,
    MenuItem.OnActionExpandListener {

    private val viewModel: LocationsViewModel by viewModels()
    private lateinit var binding: FragmentLocationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup search results ui adapter
        val adapter = LocationsAdapter(
            ::handleLocationClick,
            ::handleFavoriteLocationClick
        )
        binding.locations.adapter = adapter

        // Handle adapter paging load state updates
        adapter.addLoadStateListener(::handleLocationsLoadStates)

        // Observe view model locations paging state
        viewModel.locations.observe(viewLifecycleOwner) { adapter.submitData(lifecycle, it) }

        // Observe view model errors
        viewModel.error.observe(viewLifecycleOwner) { handleLocationsError(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate appbar menu for this fragment
        menu.clear()
        inflater.inflate(R.menu.menu_locations, menu)

        // Setup appbar search view
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView

        if (viewModel.searchViewExpanded ) {
            searchItem.expandActionView()
            searchView.setQuery(viewModel.query, false)
        }
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(this)
        searchItem.setOnActionExpandListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            when(it.isEmpty()) {
                true -> viewModel.loadFavorites()
                false -> viewModel.search(newText)
            }
        }

        return true
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        viewModel.searchViewExpanded = true
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        viewModel.searchViewExpanded = false
        return true
    }

    @VisibleForTesting
    fun handleLocationsLoadStates(state: CombinedLoadStates) {
        binding.progressBar.isVisible = state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading
    }

    private fun handleLocationClick(location: UiLocation) {
        openLocationWeather(location)
    }

    private fun handleFavoriteLocationClick(location: UiLocation) {
        when(location.isFavorite) {
            true -> unfavoriteLocation(location)
            false -> favoriteLocation(location)
        }
    }

    private fun openLocationWeather(location: UiLocation) {
        // Navigate back weather screen and pass selected result coordinates
        setFragmentResult(getString(R.string.locaiton_request_key)
            , bundleOf(
                getString(R.string.arg_lat_key) to location.lat,
                getString(R.string.arg_lon_key) to location.lon,
            )
        )
        findNavController().navigateUp()
    }

    private fun favoriteLocation(location: UiLocation) {
        viewModel.favoriteLocation(location)
    }

    private fun unfavoriteLocation(location: UiLocation) {
        AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.message_unfavorite_dialog))
            .setTitle(getString(R.string.title_unfavorite_dialog))
            .setPositiveButton(getString(R.string.title_dialog_positive_action)) { _, _ ->
                viewModel.unfavoriteLocation(location)
            }
            .setNegativeButton(getString(R.string.title_dialog_negative_action), null)
            .create()
            .show()
    }

    private fun handleLocationsError(error: AppError) {
        when(error.type) {
            ErrorType.DB_ERROR -> notifyDbError()
            ErrorType.UNKNOWN_ERR -> notifyUnknownError()
        }
    }

    private fun notifyDbError() {
        Toast.makeText(requireContext(),
            getString(R.string.text_locations_db_error),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun notifyUnknownError() {
        Toast.makeText(requireContext(),
            getString(R.string.text_unknown_error),
            Toast.LENGTH_LONG)
            .show()
    }
}