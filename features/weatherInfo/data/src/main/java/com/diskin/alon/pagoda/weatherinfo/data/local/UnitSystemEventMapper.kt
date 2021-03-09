package com.diskin.alon.pagoda.weatherinfo.data.local

import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import javax.inject.Inject

class UnitSystemEventMapper @Inject constructor() : Mapper<UnitSystemEvent, UnitSystem> {
    override fun map(source: UnitSystemEvent): UnitSystem {
        return when(source) {
            UnitSystemEvent.IMPERIAL -> UnitSystem.IMPERIAL
            UnitSystemEvent.METRIC -> UnitSystem.METRIC
        }
    }
}