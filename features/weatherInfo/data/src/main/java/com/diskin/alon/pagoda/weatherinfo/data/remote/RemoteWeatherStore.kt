package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import io.reactivex.Observable

interface RemoteWeatherStore {

    fun get(lat: Double, lon: Double): Observable<Result<LocationWeather>>
}
