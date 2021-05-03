package com.diskin.alon.pagoda.weatherinfo.domain

/**
 * Value class that hold weather info for a general hour forecast.
 */
data class HourForecast(val hour: Long,
                        val condition: WeatherCondition,
                        val temp: Double) {

    init {
        require(hour > 0)
    }
}