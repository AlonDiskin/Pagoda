package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Handles remote data loading for acquiring weather info
 */
class RemoteWeatherStoreImpl @Inject constructor(
    private val api: OpenWeatherMapApi,
    private val errorHandler: NetworkErrorHandler,
    private val mapper: Mapper2<ApiWeatherResponse, ApiLocationResponse, LocationWeather>
) : RemoteWeatherStore {

    override fun get(lat: Double, lon: Double): Observable<Result<LocationWeather>> {
        return Observable.combineLatest(
            api.getCurrentWeather(lat, lon).toObservable(),
            api.getLocationDetail(lat, lon).toObservable(),
            { weather, location -> mapper.map(weather, location.first()) })
            .subscribeOn(Schedulers.io())
            .toResult(errorHandler::handle)
    }
}