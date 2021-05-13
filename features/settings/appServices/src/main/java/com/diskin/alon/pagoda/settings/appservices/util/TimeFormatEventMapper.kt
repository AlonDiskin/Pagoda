package com.diskin.alon.pagoda.settings.appservices.util

import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit.*
import javax.inject.Inject

class TimeFormatEventMapper @Inject constructor() : Mapper<TimeFormat, TimeFormatPref> {

    override fun map(source: TimeFormat): TimeFormatPref {
        return when(source.format) {
            HourFormat.HOUR_24 -> TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24)
            HourFormat.HOUR_12 -> TimeFormatPref(TimeFormatPref.HourFormat.HOUR_12)
        }
    }
}