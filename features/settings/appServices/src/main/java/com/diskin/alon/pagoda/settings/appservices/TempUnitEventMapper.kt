package com.diskin.alon.pagoda.settings.appservices

import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit.*
import javax.inject.Inject

class TempUnitEventMapper @Inject constructor() : Mapper<Temperature, TemperatureUnitPref> {

    override fun map(source: Temperature): TemperatureUnitPref {
        return when(source.system) {
            UnitSystem.METRIC -> TemperatureUnitPref(UnitPrefSystem.METRIC)
            UnitSystem.IMPERIAL -> TemperatureUnitPref(UnitPrefSystem.IMPERIAL)
        }
    }
}