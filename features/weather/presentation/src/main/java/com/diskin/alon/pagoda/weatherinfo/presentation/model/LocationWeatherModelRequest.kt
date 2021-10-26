package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.*
import io.reactivex.Observable

sealed class LocationWeatherModelRequest(
    param: LocationWeatherRequest
) : ModelRequest<LocationWeatherRequest, Observable<AppResult<UiWeather>>>(param) {

    data class WorldLocationWeatherModelRequest(val lat: Double, val lon: Double): LocationWeatherModelRequest(WorldLocationWeatherRequest(lat, lon))

    object UserLocationWeatherModelRequest : LocationWeatherModelRequest(UserLocationWeatherRequest)
}
