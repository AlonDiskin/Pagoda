package com.diskin.alon.pagoda.locations.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.databinding.LocationSearchResultBinding
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation

/**
 * Layout adapter that display [LocationDto]s search results.
 */
class LocationSearchResultsAdapter(
    private val resultClickListener: (UiLocation) -> (Unit)
) : PagingDataAdapter<UiLocation, LocationSearchResultViewHolder>(
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

    class LocationSearchResultViewHolder(
        private val binding: LocationSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: UiLocation) {
            binding.location = location
        }
    }

    override fun onBindViewHolder(holder: LocationSearchResultViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSearchResultViewHolder {
        val binding = LocationSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        binding.resultClickListener = resultClickListener
        return LocationSearchResultViewHolder(binding)
    }
}