package com.diskin.alon.pagoda.locations.domain

/**
 * Value class for geographic location system identifier.
 */
data class Coordinates(val lat: Double, val lon: Double) {

    init {
        require(lat in -90.0..90.0)
        require(lon in -180.0..180.0)
    }
}