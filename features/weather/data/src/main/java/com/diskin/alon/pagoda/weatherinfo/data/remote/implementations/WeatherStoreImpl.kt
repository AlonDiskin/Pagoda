package com.diskin.alon.pagoda.weatherinfo.data.remote.implementations

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WeatherStoreImpl @Inject constructor(
    private val weatherApi: OpenWeatherMapApi,
    private val networkErrorHandler: NetworkErrorHandler,
    private val weatherMapper: Mapper2<ApiWeatherResponse, ApiLocationResponse, Weather>
) : WeatherStore {

    override fun getWeather(lat: Double, lon: Double): Observable<AppResult<Weather>> {
        return Observable.combineLatest(
            weatherApi.getCurrentWeather(lat, lon).toObservable(),
            weatherApi.getLocationDetail(lat, lon).toObservable(),
            { weather, location -> weatherMapper.map(weather, location.first()) })
            .subscribeOn(Schedulers.io())
            .toResult(networkErrorHandler::handle)
            .startWith(AppResult.Loading())
    }
}