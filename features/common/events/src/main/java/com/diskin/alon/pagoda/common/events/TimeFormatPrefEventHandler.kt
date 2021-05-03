package com.diskin.alon.pagoda.common.events

import android.app.Application
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref.HourFormat.HOUR_24
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref.HourFormat.valueOf
import javax.inject.Inject

class TimeFormatPrefEventHandler @Inject constructor(
    app: Application
) : SharedPrefsEventHandler<TimeFormatPref>(TimeFormatPref(HOUR_24),app) {

    override fun eventKey(): String {
        return "time format pref"
    }

    override fun convert(event: TimeFormatPref): Set<String> {
        return setOf(event.format.name)
    }

    override fun create(set: Set<String>): TimeFormatPref {
        return TimeFormatPref(valueOf(set.first()))
    }
}