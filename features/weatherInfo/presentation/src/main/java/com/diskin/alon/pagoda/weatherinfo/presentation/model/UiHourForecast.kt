package com.diskin.alon.pagoda.weatherinfo.presentation.model

import androidx.annotation.DrawableRes

/**
 * Ui info for weather hour forecast.
 */
data class UiHourForecast(val hour: String,
                          val condition: String,
                          @DrawableRes val conditionIconRes: Int,
                          val temp: String)