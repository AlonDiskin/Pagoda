package com.diskin.alon.pagoda.home.presentation

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

interface AppHomeNavProvider {

    @NavigationRes
    fun getAppNavGraph(): Int

    @IdRes
    fun getSettingsDestId(): Int

    @IdRes
    fun getSearchLocationsDestId(): Int
}
