package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WorldLocationWeatherRequest
import io.reactivex.Observable

data class WorldLocationWeatherModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<WorldLocationWeatherRequest, Observable<AppResult<UiWeather>>>(WorldLocationWeatherRequest(lat,lon))