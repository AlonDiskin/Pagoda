package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.LocationSearchResultBinding
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocationSearchResult

/**
 * Layout adapter that display [LocationDto]s search results.
 */
class LocationSearchResultsAdapter(
    private val resultClickListener: (UiLocationSearchResult) -> (Unit),
    private val addClickListener: (UiLocationSearchResult) -> (Unit)
) : PagingDataAdapter<UiLocationSearchResult, LocationSearchResultsAdapter.LocationSearchResultViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiLocationSearchResult>() {

            override fun areItemsTheSame(oldItem: UiLocationSearchResult, newItem: UiLocationSearchResult): Boolean {
                return (oldItem.lat == newItem.lat) && (oldItem.lon == newItem.lon)
            }

            override fun areContentsTheSame(oldItem: UiLocationSearchResult, newItem: UiLocationSearchResult) =
                oldItem == newItem
        }
    }

    class LocationSearchResultViewHolder(
        private val binding: LocationSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: UiLocationSearchResult) {
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
        binding.addClickListener = addClickListener

        return LocationSearchResultViewHolder(binding)
    }
}