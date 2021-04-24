package com.diskin.alon.pagoda.common.events

import android.app.Application
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem.METRIC
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem.valueOf
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import javax.inject.Inject

class WindSpeedUnitPrefEventHandler @Inject constructor(
    app: Application
) : SharedPrefsEventHandler<WindSpeedUnitPref>(WindSpeedUnitPref(METRIC),app) {

    override fun eventKey(): String {
        return "wind unit pref"
    }

    override fun convert(event: WindSpeedUnitPref): Set<String> {
        return setOf(event.system.name)
    }

    override fun create(set: Set<String>): WindSpeedUnitPref {
        return WindSpeedUnitPref(valueOf(set.first()))
    }
}