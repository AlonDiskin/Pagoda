package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import io.reactivex.Observable

/**
 * App settings repository contract.
 */
interface SettingsRepository {

    fun getTempUnit(): Observable<UnitSystemDto>

    fun getWindSpeedUnit(): Observable<UnitSystemDto>

    fun getTimeFormat(): Observable<TimeFormatDto>
}