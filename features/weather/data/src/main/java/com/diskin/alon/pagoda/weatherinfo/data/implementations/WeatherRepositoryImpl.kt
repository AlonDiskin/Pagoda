package com.diskin.alon.pagoda.weatherinfo.data.implementations

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.flatMapResult
import com.diskin.alon.pagoda.common.appservices.mapResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Handles data sources operations to provide location weather data.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val weatherStore: WeatherStore,
    private val locationProvider: UserLocationProvider,
    private val cache: WeatherCache
) : WeatherRepository {

    override fun get(lat: Double, lon: Double): Observable<AppResult<Weather>> {
        return weatherStore.getWeather(lat, lon)
    }

    override fun getCurrentLocation(): Observable<AppResult<Weather>> {
        val cachedWeather: Observable<AppResult<Weather>> = cache.getCurrentLocation()
        val updatedWeather: Observable<AppResult<Weather>> = locationProvider.getLocation()
            .flatMapResult { weatherStore.getWeather(it.lat,it.lon) }
        val updateCache: Observable<AppResult<Weather>> = updatedWeather
            .flatMapResult { weather ->
                cache.cacheCurrentLocation(weather).flatMap { updateRes ->
                    when(updateRes) {
                        is AppResult.Loading<Unit> -> Observable.just(AppResult.Loading())
                        is AppResult.Error -> Observable.just(AppResult.Error(updateRes.error))
                        else -> Observable.never()
                    }
                }
            }

        return cache.hasCurrentLocation()
            .mapResult {
                when(it) {
                    true -> 1L
                    false -> 0L
                }
            }
            .flatMapResult {
                Observable.merge(
                    cachedWeather,
                    updateCache.delaySubscription(cachedWeather.skip(it))
                )
            }
    }
}