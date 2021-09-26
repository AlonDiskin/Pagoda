package com.diskin.alon.pagoda.home.presentation

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

interface AppHomeNavProvider {

    @NavigationRes
    fun getAppNavGraph(): Int

    @IdRes
    fun getSettingsDest(): Int

    @IdRes
    fun getSearchDest(): Int

    @IdRes
    fun getFavoritesDest(): Int
}
