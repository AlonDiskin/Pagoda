package com.diskin.alon.pagoda.common.events

import io.reactivex.Observable

/**
 * [UnitSystemEvent] event provider contract.
 */
interface WeatherUnitsEventProvider {

    fun get(): Observable<UnitSystemEvent>
}