package com.diskin.alon.pagoda

import com.diskin.alon.pagoda.home.presentation.AppHomeNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import javax.inject.Inject

class AppNavigator @Inject constructor() : AppHomeNavProvider, AppLocationsNavProvider {
    override fun getAppNavGraph(): Int {
        return R.navigation.app_nav_graph
    }

    override fun getSettingsDestId(): Int {
        return R.id.action_weatherFragment_to_settingsFragment
    }

    override fun getSearchLocationsDestId(): Int {
        return R.id.action_weatherFragment_to_searchLocationFragment
    }

    override fun getWeatherDest(): Int {
        return R.id.action_searchLocationFragment_to_weatherFragment
    }
}