package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.weatherinfo.appservices.model.HourForecastDto
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.HourlyForecastBinding

class HourlyForecastAdapter(
) : ListAdapter<HourForecastDto, HourlyForecastAdapter.HourlyForecastViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HourForecastDto>() {

            override fun areItemsTheSame(oldItem: HourForecastDto, newItem: HourForecastDto): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: HourForecastDto, newItem: HourForecastDto) =
                oldItem == newItem
        }
    }

    class HourlyForecastViewHolder(
        private val binding: HourlyForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: HourForecastDto) {
            binding.forecast = forecast
        }
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = HourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return HourlyForecastViewHolder(binding)
    }
}