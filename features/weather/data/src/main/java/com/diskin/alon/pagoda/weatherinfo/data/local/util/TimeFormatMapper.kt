package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import javax.inject.Inject

class TimeFormatMapper @Inject constructor() : Mapper<TimeFormat, TimeFormatDto> {

    override fun map(source: TimeFormat): TimeFormatDto {
        return when(source.format) {
            TimeFormat.HourFormat.HOUR_12 -> TimeFormatDto.HOUR_12
            TimeFormat.HourFormat.HOUR_24 -> TimeFormatDto.HOUR_24
        }
    }
}