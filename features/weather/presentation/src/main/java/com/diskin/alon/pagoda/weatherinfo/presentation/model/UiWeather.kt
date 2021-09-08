package com.diskin.alon.pagoda.weatherinfo.presentation.model

import androidx.annotation.DrawableRes

/**
 * Ui info for weather.
 */
data class UiWeather(val locationName: String,
                     val timeZone: String,
                     val clock24HourFormat: String?,
                     val clock12HourFormat: String?,
                     val currentTemp: String,
                     val weatherCondition: String,
                     @DrawableRes val conditionIconRes: Int,
                     val feelTemp: String,
                     val minMaxTemp: String,
                     val humidity: String,
                     val windSpeed: String,
                     val sunrise: String,
                     val sunset: String,
                     val uvIndex: String,
                     val hourlyForecast: List<UiHourForecast>,
                     val dailyForecast: List<UiDayForecast>,
                     val updated: String,
                     val currentTempUnit: String)