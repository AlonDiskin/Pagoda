package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable

/**
 * [Weather] repository contract
 */
interface WeatherRepository {

    /**
     * Get a [Weather] that holds the weather info for given location. Unit system
     * for data is metric.
     *
     * @param lat latitude value of location.
     * @param lon longitude value of location.
     */
    fun getLocationWeather(lat: Double, lon: Double): Observable<AppResult<Weather>>

    /**
     * Get a [Weather] that holds the weather info for current device location. Unit system
     * for data is metric.
     */
    fun getCurrentLocationWeather(): Observable<AppResult<Weather>>
}