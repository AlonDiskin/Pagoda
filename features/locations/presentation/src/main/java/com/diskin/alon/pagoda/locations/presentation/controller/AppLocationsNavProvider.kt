package com.diskin.alon.pagoda.locations.presentation.controller

import androidx.annotation.IdRes

/**
 * App navigation functionality contract for locations bounded context.
 */
interface AppLocationsNavProvider {

    @IdRes
    fun getSearchLocationsToWeatherDataNavRoute(): Int

    @IdRes
    fun getBookmarkedLocationsToWeatherDataNavRoute(): Int

    @IdRes
    fun getBookmarkedLocationsLocationsSearchNavRoute(): Int
}