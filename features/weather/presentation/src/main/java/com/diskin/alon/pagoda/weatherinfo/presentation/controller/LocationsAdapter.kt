package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.LocationBinding
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocation

/**
 * Layout adapter that display [UiLocation]s search results.
 */
class LocationsAdapter(
    private val locationClickListener: (UiLocation) -> (Unit),
    private val favoriteClickListener: (UiLocation) -> (Unit)
) : PagingDataAdapter<UiLocation, LocationsAdapter.LocationViewHolder>(
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

    class LocationViewHolder(
        private val binding: LocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: UiLocation) {
            binding.location = location
        }
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = LocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.locationClickListener = locationClickListener
        binding.favoriteClickListener = favoriteClickListener

        return LocationViewHolder(binding)
    }
}