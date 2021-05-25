package com.diskin.alon.pagoda.locations.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.model.UiBookmarkedLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.BookmarkedLocationsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

@OptionalInject
@AndroidEntryPoint
class BookmarkedLocationsFragment : Fragment() {

    private val viewModel: BookmarkedLocationsViewModel by viewModels()
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
        return inflater.inflate(R.layout.fragment_bookmarked_locations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set locations adapter
        val rv = view.findViewById<RecyclerView>(R.id.bookmarked_locations)
        val adapter = BookmarkedLocationsAdapter(::handleLocationClick,::handleLocationOptionsClick)
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

        // Observe view model errors
        viewModel.error.observe(viewLifecycleOwner) { handleLocationsError(it) }

        // Handle floating action button click
        val fab = view.findViewById<FloatingActionButton>(R.id.add_fab)
        fab.setOnClickListener {
            findNavController().navigate(appNav.getBookmarkedLocationsLocationsSearchNavRoute())
        }

        // Hide/Show fab upon rv scrolling
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })
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
}