package com.diskin.alon.pagoda.weatherinfo.presentation.model

import androidx.annotation.DrawableRes

/**
 * Ui info for weather day forecast.
 */
data class UiDayForecast(val dayOfWeek: String,
                         val condition: String,
                         @DrawableRes val conditionIconRes: Int,
                         val minTemp: String,
                         val maxTemp: String)