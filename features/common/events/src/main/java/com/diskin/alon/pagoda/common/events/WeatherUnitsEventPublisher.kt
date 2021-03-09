package com.diskin.alon.pagoda.common.events

/**
 * [UnitSystemEvent] event publisher contract.
 */
interface WeatherUnitsEventPublisher {

    fun post(event: UnitSystemEvent)
}