package com.diskin.alon.pagoda.weatherinfo.data.remote.implementations

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.appservices.toSingleResult
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class WeatherStoreImpl @Inject constructor(
    private val weatherApi: OpenWeatherMapApi,
    private val networkErrorHandler: NetworkErrorHandler,
    private val weatherMapper: Mapper2<ApiWeatherResponse, ApiLocationResponse, Weather>
) : WeatherStore {

    override fun getWeather(lat: Double, lon: Double): Single<Result<Weather>> {
        return Single.fromObservable(
            Observable.combineLatest(
                weatherApi.getCurrentWeather(lat, lon).toObservable(),
                weatherApi.getLocationDetail(lat, lon).toObservable(),
                { weather, location -> weatherMapper.map(weather, location.first()) })
        )
            .subscribeOn(Schedulers.io())
            .toSingleResult(networkErrorHandler::handle)
    }

    override fun isUpdateAvailable(lastUpdate: Long): Boolean {
        val current = Calendar.getInstance().timeInMillis
        val lastUpdated =  LocalDateTime(lastUpdate)
        val nextUpdate = LocalDateTime(
            lastUpdated.year,
            lastUpdated.monthOfYear,
            lastUpdated.dayOfMonth,
            lastUpdated.plusHours(1).hourOfDay,
            0
        ).toDate().time

        return current >= nextUpdate
    }
}