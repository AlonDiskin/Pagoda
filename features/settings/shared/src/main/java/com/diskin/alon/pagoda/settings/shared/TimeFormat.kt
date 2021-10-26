package com.diskin.alon.pagoda.settings.shared

/**
 * Hold the data associated with time format selection.
 */
data class TimeFormat(val format: HourFormat) {
    enum class HourFormat { HOUR_12,HOUR_24 }
}