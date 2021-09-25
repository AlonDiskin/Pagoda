package com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Single

interface WeatherStore {

    fun getWeather(lat: Double, lon: Double): Single<Result<Weather>>

    fun isUpdateAvailable(lastUpdate: Long): Boolean
}