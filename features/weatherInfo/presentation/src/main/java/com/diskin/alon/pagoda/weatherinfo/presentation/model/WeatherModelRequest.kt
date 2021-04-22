package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.*
import io.reactivex.Observable

sealed class WeatherModelRequest(request: LocationWeatherRequest)
    : ModelRequest<LocationWeatherRequest, Observable<Result<LocationWeatherDto>>>(request) {

    object CurrentLocationWeatherModelRequest : WeatherModelRequest(CurrentLocationRequest)

    data class LocationWeatherModelRequest(val lat: Double, val lon: Double): WeatherModelRequest(LocationRequest(lat, lon))
}