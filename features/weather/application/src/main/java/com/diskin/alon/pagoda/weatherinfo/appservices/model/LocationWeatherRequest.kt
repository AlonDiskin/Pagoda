package com.diskin.alon.pagoda.weatherinfo.appservices.model

sealed class LocationWeatherRequest {

    data class WorldLocationWeatherRequest(val lat: Double, val lon: Double): LocationWeatherRequest()

    object UserLocationWeatherRequest : LocationWeatherRequest()
}
