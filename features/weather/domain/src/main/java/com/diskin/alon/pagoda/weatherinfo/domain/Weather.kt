package com.diskin.alon.pagoda.weatherinfo.domain

import kotlin.math.roundToInt

/**
 * Entity class that represent the distinct weather conditions and info for a geographical location.
 */
data class Weather(val id: Coordinates,
                   var name: String,
                   var country: String,
                   var timeZone: String,
                   var currentTemp: Double,
                   var feelTemp: Double,
                   var minTemp: Double,
                   var maxTemp: Double,
                   var condition: WeatherCondition,
                   var humidity: Double,
                   var windSpeed: Double,
                   var sunrise: Long,
                   var sunset: Long,
                   var uvIndexValue: Double,
                   var unitSystem: UnitSystem,
                   var hourlyForecast: List<HourForecast>,
                   var dailyForecast: List<DayForecast>,
                   var updated: Long) {

    init {
        require(humidity >= 0)
        require(windSpeed >= 0)
        require(name.isNotEmpty())
        require(timeZone.isNotEmpty())
        require(sunrise > 0)
        require(sunset > 0)
        require(sunrise != sunset)
        require(uvIndexValue >= 0)
        require(hourlyForecast.size == 12)
        require(dailyForecast.size == 8)
        require(updated > 0)
    }

    /**
     * Changes all current data to the imperial unit system.
     */
    fun toImperial() {
        if (unitSystem == UnitSystem.METRIC) {
            currentTemp = celsiusToFahrenheit(currentTemp)
            feelTemp = celsiusToFahrenheit(feelTemp)
            minTemp = celsiusToFahrenheit(minTemp)
            maxTemp = celsiusToFahrenheit(maxTemp)
            windSpeed = kphToMph(windSpeed)
            unitSystem = UnitSystem.IMPERIAL
            hourlyForecast = hourlyForecast.map {
                HourForecast(
                    it.hour,
                    it.condition,
                    celsiusToFahrenheit(it.temp)
                )
            }
            dailyForecast = dailyForecast.map {
                DayForecast(
                    it.dayOfWeek,
                    celsiusToFahrenheit(it.forecastTemp),
                    it.condition,
                    celsiusToFahrenheit(it.minTemp),
                    celsiusToFahrenheit(it.maxTemp)
                )
            }
        }
    }

    /**
     * Changes all current data to the metric unit system.
     */
    fun toMetric() {
        if (unitSystem == UnitSystem.IMPERIAL) {
            currentTemp = fahrenheitToCelsius(currentTemp)
            feelTemp = fahrenheitToCelsius(feelTemp)
            minTemp = fahrenheitToCelsius(minTemp)
            maxTemp = fahrenheitToCelsius(maxTemp)
            windSpeed = mphToKph(windSpeed)
            unitSystem = UnitSystem.METRIC
            hourlyForecast = hourlyForecast.map {
                HourForecast(
                    it.hour,
                    it.condition,
                    fahrenheitToCelsius(it.temp)
                )
            }
            dailyForecast = dailyForecast.map {
                DayForecast(
                    it.dayOfWeek,
                    fahrenheitToCelsius(it.forecastTemp),
                    it.condition,
                    fahrenheitToCelsius(it.minTemp),
                    fahrenheitToCelsius(it.maxTemp)
                )
            }
        }
    }

    fun uvIndex(): UvIndex {
        return when {
            (uvIndexValue < 3) -> UvIndex.LOW
            (uvIndexValue >= 3 && uvIndexValue < 6) -> UvIndex.MODERATE
            (uvIndexValue >= 6 && uvIndexValue < 8) -> UvIndex.HIGH
            else -> UvIndex.VERY_HIGH
        }
    }

    private fun celsiusToFahrenheit(celsius: Double): Double {
        return ((celsius * (9.0 / 5.0)) + 32).roundToInt().toDouble()
    }

    private fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return ((fahrenheit - 32) * (5.0 / 9.0)).roundToInt().toDouble()
    }

    private fun kphToMph(kph: Double): Double {
        return (kph / 1.6).roundToInt().toDouble()
    }

    private fun mphToKph(mph: Double): Double {
        return (mph * 1.6).roundToInt().toDouble()
    }
}