package com.diskin.alon.pagoda.locations.presentation.controller

import androidx.annotation.IdRes

/**
 * App navigation functionality contract for locations bounded context.
 */
interface AppLocationsNavProvider {

    @IdRes
    fun getWeatherDest(): Int

    @IdRes
    fun getLocationsSearchDest(): Int
}