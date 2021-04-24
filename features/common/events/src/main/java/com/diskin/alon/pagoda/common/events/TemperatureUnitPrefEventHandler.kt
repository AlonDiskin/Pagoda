package com.diskin.alon.pagoda.common.events

import android.app.Application
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem.METRIC
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem.valueOf
import javax.inject.Inject

class TemperatureUnitPrefEventHandler @Inject constructor(
    app: Application
) : SharedPrefsEventHandler<TemperatureUnitPref>(TemperatureUnitPref(METRIC),app) {

    override fun eventKey(): String {
        return "temperature unit pref"
    }

    override fun convert(event: TemperatureUnitPref): Set<String> {
        return setOf(event.system.name)
    }

    override fun create(set: Set<String>): TemperatureUnitPref {
        return TemperatureUnitPref(valueOf(set.first()))
    }
}