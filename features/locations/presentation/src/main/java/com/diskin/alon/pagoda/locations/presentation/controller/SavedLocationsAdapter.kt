package com.diskin.alon.pagoda.locations.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsAdapter.SavedLocationViewHolder
import com.diskin.alon.pagoda.locations.presentation.databinding.SavedLocationBinding
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation

/**
 * Layout adapter that display [UiLocation]s for user saved locations.
 */
class SavedLocationsAdapter(
    private val locationClickListener: (UiLocation) -> (Unit)
) : PagingDataAdapter<UiLocation, SavedLocationViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiLocation>() {

            override fun areItemsTheSame(oldItem: UiLocation, newItem: UiLocation): Boolean {
                return (oldItem.lat == newItem.lat) && (oldItem.lon == newItem.lon)
            }

            override fun areContentsTheSame(oldItem: UiLocation, newItem: UiLocation) =
                oldItem == newItem
        }
    }

    class SavedLocationViewHolder(
        private val binding: SavedLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: UiLocation) {
            binding.location = location
        }
    }

    override fun onBindViewHolder(holder: SavedLocationViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedLocationViewHolder {
        val binding = SavedLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        binding.locationClickListener = locationClickListener
        return SavedLocationViewHolder(binding)
    }
}