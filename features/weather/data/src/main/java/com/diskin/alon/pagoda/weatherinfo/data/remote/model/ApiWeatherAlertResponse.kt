package com.diskin.alon.pagoda.weatherinfo.data.remote.model

data class ApiWeatherAlertResponse(val alerts: List<Alert>?) {

    data class Alert(val event: String)
}