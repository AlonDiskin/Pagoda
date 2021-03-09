package com.diskin.alon.pagoda.home.presentation

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

interface AppNavGraphProvider {

    @NavigationRes
    fun getAppNavGraph(): Int

    @IdRes
    fun getSettingsGraphId(): Int
}
