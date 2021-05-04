package com.diskin.alon.pagoda.settings.appservices.util

import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit.*
import javax.inject.Inject

class WindSpeedUnitEventMapper @Inject constructor() : Mapper<WindSpeed, WindSpeedUnitPref> {

    override fun map(source: WindSpeed): WindSpeedUnitPref {
        return when(source.system) {
            UnitSystem.METRIC -> WindSpeedUnitPref(UnitPrefSystem.METRIC)
            UnitSystem.IMPERIAL -> WindSpeedUnitPref(UnitPrefSystem.IMPERIAL)
        }
    }
}