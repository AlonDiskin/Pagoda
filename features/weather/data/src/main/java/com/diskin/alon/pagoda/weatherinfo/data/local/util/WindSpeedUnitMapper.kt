package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import javax.inject.Inject

class WindSpeedUnitMapper @Inject constructor() : Mapper<WindSpeedUnit, UnitSystemDto> {

    override fun map(source: WindSpeedUnit): UnitSystemDto {
        return when(source.system) {
            UnitSystem.METRIC -> UnitSystemDto.METRIC
            UnitSystem.IMPERIAL -> UnitSystemDto.IMPERIAL
        }
    }
}