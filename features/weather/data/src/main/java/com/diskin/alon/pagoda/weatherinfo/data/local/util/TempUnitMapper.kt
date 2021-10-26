package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import javax.inject.Inject

class TempUnitMapper @Inject constructor() : Mapper<TempUnit, UnitSystemDto> {

    override fun map(source: TempUnit): UnitSystemDto {
        return when(source.system) {
            UnitSystem.METRIC -> UnitSystemDto.METRIC
            UnitSystem.IMPERIAL -> UnitSystemDto.IMPERIAL
        }
    }
}