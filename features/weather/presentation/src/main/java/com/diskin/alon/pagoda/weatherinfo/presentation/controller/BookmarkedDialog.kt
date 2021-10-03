package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.FragmentBookmarkedLocationsBinding
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiBookmarkedLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.BookmarkedLocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@OptionalInject
@AndroidEntryPoint
class BookmarkedDialog : DialogFragment() {

    private val viewModel: BookmarkedLocationsViewModel by viewModels()
    private lateinit var binding: FragmentBookmarkedLocationsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            binding = FragmentBookmarkedLocationsBinding.inflate(inflater,null,false)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(binding.root)
            builder.setTitle("Locations")
            val dialog = builder.create()

            // Set locations adapter
            val adapter = BookmarkedLocationsAdapter(
                ::handleLocationClick,
                ::handleLocationOptionsClick
            )
            binding.bookmarkedLocations.adapter = adapter

            // Handle adapter paging load state updates
            adapter.addLoadStateListener(::handleBookmarksLoadStates)

            // Observe view model locations paging
            viewModel.locations.observe(it) {
                adapter.submitData(lifecycle, it)
            }


            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun handleLocationClick(location: UiBookmarkedLocation) {
        // Navigate back to weather screen and pass selected location coordinates
        setFragmentResult(getString(R.string.locaiton_request_key)
            , bundleOf(
                getString(R.string.arg_lat_key) to location.lat,
                getString(R.string.arg_lon_key) to location.lon,
            )
        )

        findNavController().navigateUp()
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