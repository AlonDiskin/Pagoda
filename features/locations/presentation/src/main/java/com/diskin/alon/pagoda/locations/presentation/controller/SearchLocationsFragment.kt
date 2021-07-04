package com.diskin.alon.pagoda.locations.presentation.controller

import android.os.Bundle
import android.view.*
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.databinding.FragmentSearchLocationsBinding
import com.diskin.alon.pagoda.locations.presentation.model.UiLocationSearchResult
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SearchLocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

@OptionalInject
@AndroidEntryPoint
class SearchLocationsFragment : Fragment(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private val viewModel: SearchLocationsViewModel by viewModels()
    private lateinit var binding: FragmentSearchLocationsBinding
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
        binding = FragmentSearchLocationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup search results ui adapter
        val adapter = LocationSearchResultsAdapter(
            ::handleResultClick,
            ::handleAddResultClick
        )
        binding.searchLocationResults.adapter = adapter

        // Handle adapter paging load state updates
        adapter.addLoadStateListener(::handleSearchResultsLoadStates)

        // Observe view model search results paging state
        viewModel.results.observe(viewLifecycleOwner) { adapter.submitData(lifecycle, it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate appbar menu for this fragment
        menu.clear()
        inflater.inflate(R.menu.menu_search_location, menu)

        // Setup appbar search view
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView

        searchItem.expandActionView()
        searchView.setIconifiedByDefault(false)
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setQuery(viewModel.query, false)
        searchView.setOnQueryTextListener(this)
        searchItem.setOnActionExpandListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Perform search
        newText?.let { viewModel.search(newText) }
        return true
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        // Navigate back to parent dest
        findNavController().navigateUp()
        return true
    }

    private fun handleResultClick(result: UiLocationSearchResult) {
        // Navigate to weather info screen and pass selected result coordinates
        val bundle = bundleOf(LOCATION_LAT to result.lat, LOCATION_LON to result.lon)
        findNavController().navigate(appNav.getSearchLocationsToWeatherDataNavRoute(), bundle)
    }

    private fun handleAddResultClick(result: UiLocationSearchResult) {
        viewModel.addToBookmarked(result)
    }

    @VisibleForTesting
    fun handleSearchResultsLoadStates(state: CombinedLoadStates) {
        binding.progressBar.isVisible = state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading
    }
}