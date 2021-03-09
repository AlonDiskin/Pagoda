package com.diskin.alon.pagoda.weatherinfo.data.implementations

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.AppPrefsStore
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Handles data sources operations to provide app preference.
 */
class AppPrefsStoreImpl @Inject constructor(
    private val eventProvider: WeatherUnitsEventProvider,
    private val mapper: Mapper<UnitSystemEvent,UnitSystem>
) : AppPrefsStore {

    override fun getUnitSystem(): Observable<Result<UnitSystem>> {
        return eventProvider
            .get()
            .map(mapper::map)
            .toResult()
    }
}