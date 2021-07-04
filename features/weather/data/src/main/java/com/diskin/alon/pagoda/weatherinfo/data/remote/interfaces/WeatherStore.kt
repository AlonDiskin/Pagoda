package com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable

interface WeatherStore {

    fun getWeather(lat: Double, lon: Double): Observable<AppResult<Weather>>
}