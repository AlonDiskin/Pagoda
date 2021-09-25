package com.diskin.alon.pagoda.locations.presentation.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsAdapter.BookmarkedLocationViewHolder
import com.diskin.alon.pagoda.locations.presentation.databinding.BookmarkedLocationBinding
import com.diskin.alon.pagoda.locations.presentation.model.UiBookmarkedLocation

/**
 * Layout adapter that display [UiBookmarkedLocation]s for user saved locations.
 */
class BookmarkedLocationsAdapter(
    private val locationClickListener: (UiBookmarkedLocation) -> (Unit),
    private val optionsClickListener: (UiBookmarkedLocation, View) -> (Unit)
) : PagingDataAdapter<UiBookmarkedLocation, BookmarkedLocationViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiBookmarkedLocation>() {

            override fun areItemsTheSame(oldItem: UiBookmarkedLocation, newItem: UiBookmarkedLocation): Boolean {
                return (oldItem.lat == newItem.lat) && (oldItem.lon == newItem.lon)
            }

            override fun areContentsTheSame(oldItem: UiBookmarkedLocation, newItem: UiBookmarkedLocation) =
                oldItem == newItem
        }
    }

    class BookmarkedLocationViewHolder(
        private val binding: BookmarkedLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: UiBookmarkedLocation) {
            binding.location = location
        }
    }

    override fun onBindViewHolder(holder: BookmarkedLocationViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkedLocationViewHolder {
        val binding = BookmarkedLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.locationClickListener = locationClickListener
        binding.optionsClickListener = optionsClickListener

        return BookmarkedLocationViewHolder(binding)
    }
}