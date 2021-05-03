package com.diskin.alon.pagoda.settings.presentation

import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit

data class UpdateWeatherUnitModelRequest(
    val unit: WeatherUnit
) : ModelRequest<WeatherUnit,Unit>(unit)