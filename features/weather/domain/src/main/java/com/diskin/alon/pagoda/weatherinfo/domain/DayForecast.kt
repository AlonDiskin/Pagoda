package com.diskin.alon.pagoda.weatherinfo.domain

/**
 * Value class that hold weather info for a general day forecast.
 */
data class DayForecast(val dayOfWeek: Int,
                       val forecastTemp: Double,
                       val condition: WeatherCondition,
                       val minTemp: Double,
                       val maxTemp: Double){

    init {
        require(dayOfWeek in 1..7)
    }
}