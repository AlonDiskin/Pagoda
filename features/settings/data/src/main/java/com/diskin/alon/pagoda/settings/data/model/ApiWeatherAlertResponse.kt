package com.diskin.alon.pagoda.settings.data.model

data class ApiWeatherAlertResponse(val alerts: List<Alert>?) {

    data class Alert(val event: String)
}