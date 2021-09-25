package com.diskin.alon.pagoda.weatherinfo.data.local.interfaces

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import io.reactivex.Single

interface WeatherCache {

    fun getCurrentLocationWeather(): Observable<Result<Weather>>

    fun cacheCurrentLocation(weather: Weather): Single<Result<Unit>>

    fun hasCurrentLocation(): Single<Result<Boolean>>
}