package com.diskin.alon.pagoda.common.eventcontracts.settings

data class TimeFormatPref(val format: HourFormat) {
    enum class HourFormat { HOUR_12,HOUR_24 }
}