package com.diskin.alon.pagoda.weatherinfo.data.implementations

import com.diskin.alon.pagoda.common.appservices.*
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
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

    override fun getLocationWeather(lat: Double, lon: Double): Observable<AppResult<Weather>> {
        return weatherStore.getWeather(lat, lon)
            .singleResultToLoadingAppResult()
    }

    override fun getCurrentLocationWeather(): Observable<AppResult<Weather>> {
        val cachedWeather = cache.getCurrentLocationWeather().toLoadingAppResult()

        return cache.hasCurrentLocation()
            .singleResultToAppResult()
            .flatMapAppResult {
                Observable.merge(
                    cachedWeather,
                    updateCachedWeather(it)
                    .delaySubscription(cachedWeather.skip(calcCachedSkip(it)))
                )
            }
    }

    private fun calcCachedSkip(hasCachedWeather: Boolean): Long {
        return when(hasCachedWeather) {
            true -> 1L
            false -> 0L
        }
    }

    private fun updateCachedWeather(hasCachedWeather: Boolean): Observable<AppResult<Weather>> {
        return locationProvider.getLocation()
            .singleResultToAppResult()
            .flatMapAppResult { location ->
                when(hasCachedWeather) {
                    true -> {
                        cache.getCurrentLocationWeather().firstOrError()
                            .singleResultToAppResult()
                            .flatMapAppResult { weather ->
                                when(isSameLocation(weather, location)) {
                                    true -> {
                                        if (weatherStore.isUpdateAvailable(weather.updated)) {
                                            updateCache(location).startWith(AppResult.Loading())
                                        } else {
                                            Observable.never()
                                        }
                                    }

                                    false -> updateCache(location).startWith(AppResult.Loading())
                                }
                            }
                    }
                    false -> updateCache(location)
                }
            }
    }

    private fun updateCache(location: UserLocation): Observable<AppResult<Weather>> {
        return weatherStore.getWeather(location.lat,location.lon)
            .flatMapResult { cache.cacheCurrentLocation(it) }
            .toObservable()
            .flatMap {
                when(it) {
                    is Result.Error -> Observable.just(AppResult.Error(it.error))
                    else -> Observable.never()
                }
            }
    }

    private fun isSameLocation(weather: Weather,location: UserLocation): Boolean {
        return weather.id.lat == location.lat && weather.id.lon == location.lon
    }
}