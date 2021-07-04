package com.diskin.alon.pagoda.weatherinfo.data.local.interfaces

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable

interface WeatherCache {

    fun getCurrentLocation(): Observable<AppResult<Weather>>

    fun cacheCurrentLocation(weather: Weather): Observable<AppResult<Unit>>

    fun hasCurrentLocation(): Observable<AppResult<Boolean>>
}