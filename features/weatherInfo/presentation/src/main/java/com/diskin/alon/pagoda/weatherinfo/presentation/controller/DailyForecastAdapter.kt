package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.pagoda.weatherinfo.appservices.model.DayForecastDto
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.DailyForecastBinding

class DailyForecastAdapter(
) : ListAdapter<DayForecastDto, DailyForecastAdapter.DailyForecastViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DayForecastDto>() {

            override fun areItemsTheSame(oldItem: DayForecastDto, newItem: DayForecastDto): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DayForecastDto, newItem: DayForecastDto) =
                oldItem == newItem
        }
    }

    class DailyForecastViewHolder(
        private val binding: DailyForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: DayForecastDto) {
            binding.forecast = forecast
        }
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = DailyForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return DailyForecastViewHolder(binding)
    }
}