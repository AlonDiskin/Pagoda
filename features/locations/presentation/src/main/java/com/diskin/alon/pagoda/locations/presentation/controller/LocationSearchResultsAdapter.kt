package com.diskin.alon.pagoda.locations.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.databinding.LocationSearchResultBinding

/**
 * Layout adapter that display [LocationSearchResult]s data.
 */
class LocationSearchResultsAdapter(
    private val resultClickListener: (LocationSearchResult) -> (Unit)
) : PagingDataAdapter<LocationSearchResult, LocationSearchResultViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LocationSearchResult>() {

            override fun areItemsTheSame(oldItem: LocationSearchResult, newItem: LocationSearchResult): Boolean {
                return (oldItem.lat == newItem.lat) && (oldItem.lon == newItem.lon)
            }

            override fun areContentsTheSame(oldItem: LocationSearchResult, newItem: LocationSearchResult) =
                oldItem == newItem
        }
    }

    class LocationSearchResultViewHolder(
        private val binding: LocationSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: LocationSearchResult) {
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