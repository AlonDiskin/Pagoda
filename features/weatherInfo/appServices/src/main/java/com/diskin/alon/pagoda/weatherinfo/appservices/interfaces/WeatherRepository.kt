package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import io.reactivex.Observable

/**
 * [LocationWeather] repository contract
 */
interface WeatherRepository {

    /**
     * Get a [LocationWeather] that holds the weather info for given location. Unit system
     * for data is metric.
     *
     * @param lat latitude value of location.
     * @param lon longitude value of location.
     */
    fun get(lat: Double, lon: Double): Observable<Result<LocationWeather>>
}