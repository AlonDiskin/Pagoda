package com.diskin.alon.pagoda.weatherinfo.appservices.model

sealed class LocationWeatherRequest {

    object CurrentLocationRequest : LocationWeatherRequest()

    data class LocationRequest(val lat: Double, val lon: Double) : LocationWeatherRequest()
}