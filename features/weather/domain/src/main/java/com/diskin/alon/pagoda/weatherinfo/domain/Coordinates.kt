package com.diskin.alon.pagoda.weatherinfo.domain

/**
 * Value class for geographic location system identifier.
 */
data class Coordinates(val lat: Double, val lon: Double) {

    init {
        require(lat >= -90.0 && lat <= 90.0)
        require(lon >= -180.0 && lon <= 180.0)
    }
}