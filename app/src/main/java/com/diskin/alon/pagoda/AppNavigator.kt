package com.diskin.alon.pagoda

import com.diskin.alon.pagoda.home.presentation.AppNavGraphProvider
import javax.inject.Inject

class AppNavigator @Inject constructor() : AppNavGraphProvider {
    override fun getAppNavGraph(): Int {
        return R.navigation.app_nav_graph
    }

    override fun getSettingsGraphId(): Int {
        return R.id.action_weatherFragment_to_settings
    }
}