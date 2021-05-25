package com.diskin.alon.pagoda.home.presentation

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

interface AppHomeNavProvider {

    @NavigationRes
    fun getAppNavGraph(): Int

    @IdRes
    fun getWeatherDataToSettingsNavRoute(): Int

    @IdRes
    fun getWeatherDataSearchLocationsNavRoute(): Int

    @IdRes
    fun getWeatherDataToSavedLocationsNavRoute(): Int
}
