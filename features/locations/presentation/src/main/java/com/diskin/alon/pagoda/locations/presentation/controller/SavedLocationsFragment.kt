package com.diskin.alon.pagoda.locations.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SavedLocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

@OptionalInject
@AndroidEntryPoint
class SavedLocationsFragment : Fragment() {

    private val viewModel: SavedLocationsViewModel by viewModels()
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
        return inflater.inflate(R.layout.fragment_saved_locations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set locations adapter
        val rv = view.findViewById<RecyclerView>(R.id.saved_locations)
        val adapter = SavedLocationsAdapter(::handleResultClick)
        rv.adapter = adapter

        // Handle adapter paging load state updates
        adapter.addLoadStateListener { state ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            when(state.refresh) {
                is LoadState.Loading -> progressBar.visibility = View.VISIBLE

                is LoadState.NotLoading -> {
                    if (state.append is LoadState.NotLoading) {
                        progressBar.visibility = View.GONE
                    }
                }
            }

            when (state.append) {
                is LoadState.Loading -> progressBar.visibility = View.VISIBLE

                is LoadState.NotLoading -> {
                    if (state.refresh is LoadState.NotLoading) {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // Observe view model locations paging
        viewModel.locations.observe(viewLifecycleOwner) { adapter.submitData(lifecycle, it) }
    }

    private fun handleResultClick(location: UiLocation) {
        // Navigate to weather info screen and pass selected location coordinates
        val bundle = bundleOf(LOCATION_LAT to location.lat, LOCATION_LON to location.lon)
        findNavController().navigate(appNav.getWeatherDest(), bundle)
    }
}