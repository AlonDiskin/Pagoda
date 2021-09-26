package com.diskin.alon.pagoda.locations.presentation.controller

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.databinding.FragmentBookmarkedLocationsBinding
import com.diskin.alon.pagoda.locations.presentation.model.UiBookmarkedLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.BookmarkedLocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

@OptionalInject
@AndroidEntryPoint
class BookmarkedLocationsFragment : Fragment() {

    private val viewModel: BookmarkedLocationsViewModel by viewModels()
    private lateinit var binding: FragmentBookmarkedLocationsBinding
    @Inject
    lateinit var appNav: AppLocationsNavProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarkedLocationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set locations adapter
        val adapter = BookmarkedLocationsAdapter(
            ::handleLocationClick,
            ::handleLocationOptionsClick
        )
        binding.bookmarkedLocations.adapter = adapter

        // Handle adapter paging load state updates
        adapter.addLoadStateListener(::handleBookmarksLoadStates)

        // Observe view model locations paging
        viewModel.locations.observe(viewLifecycleOwner) { adapter.submitData(lifecycle, it) }

        // Observe view model errors
        viewModel.error.observe(viewLifecycleOwner) { handleLocationsError(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_favorite_locations, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add -> {
                findNavController().navigate(appNav.getBookmarkedLocationsLocationsSearchNavRoute())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLocationsError(error: AppError) {
        when(error.type) {
            ErrorType.DB_ERROR -> notifyDbError()
        }
    }

    private fun notifyDbError() {
        Toast.makeText(requireContext(),
            getString(R.string.text_locations_db_error),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun handleLocationClick(location: UiBookmarkedLocation) {
        // Navigate to weather info screen and pass selected location coordinates
        val bundle = bundleOf(LOCATION_LAT to location.lat, LOCATION_LON to location.lon)
        findNavController().navigate(appNav.getBookmarkedLocationsToWeatherDataNavRoute(), bundle)
    }

    private fun handleLocationOptionsClick(location: UiBookmarkedLocation, view: View) {
        PopupMenu(requireActivity(), view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete_location -> {
                        removeLocation(location)
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.menu_location)
            show()
        }
    }

    private fun removeLocation(location: UiBookmarkedLocation) {
        AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.message_delete_location_dialog))
            .setTitle(getString(R.string.title_delete_location_dialog))
            .setPositiveButton(getString(R.string.title_dialog_positive_action)) { _, _ ->
                viewModel.deleteSavedLocation(location)
            }
            .setNegativeButton(getString(R.string.title_dialog_negative_action), null)
            .create()
            .show()
    }

    @VisibleForTesting
    fun handleBookmarksLoadStates(state: CombinedLoadStates) {
        binding.progressBar.isVisible = state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading
    }
}