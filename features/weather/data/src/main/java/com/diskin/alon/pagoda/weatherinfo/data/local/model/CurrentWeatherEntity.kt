package com.diskin.alon.pagoda.weatherinfo.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diskin.alon.pagoda.weatherinfo.domain.DayForecast
import com.diskin.alon.pagoda.weatherinfo.domain.HourForecast
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherCondition

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(val lat: Double,
                                val lon: Double,
                                val name: String,
                                val country: String,
                                val timeZone: String,
                                val currentTemp: Double,
                                val feelTemp: Double,
                                val minTemp: Double,
                                val maxTemp: Double,
                                val humidity: Double,
                                val windSpeed: Double,
                                val sunrise: Long,
                                val sunset: Long,
                                val uvIndexValue: Double,
                                val condition: WeatherCondition,
                                val unitSystem: UnitSystem,
                                val hourlyForecast: List<HourForecast>,
                                val dailyForecast: List<DayForecast>,
                                val updated: Long,
                                @PrimaryKey val id: Int = WEATHER_ID) {
    companion object {
        const val WEATHER_ID = 1
    }
}