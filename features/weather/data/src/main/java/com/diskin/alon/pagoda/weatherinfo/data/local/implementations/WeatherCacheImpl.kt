package com.diskin.alon.pagoda.weatherinfo.data.local.implementations

import com.diskin.alon.pagoda.common.appservices.*
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity.Companion.WEATHER_ID
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import javax.inject.Inject

class WeatherCacheImpl @Inject constructor(
    private val weatherDao: CurrentWeatherDao,
    private val weatherMapper: Mapper<CurrentWeatherEntity,Weather>,
    private val entityMapper: Mapper<Weather,CurrentWeatherEntity>
) : WeatherCache {

    override fun getCurrentLocation(): Observable<AppResult<Weather>> {
        return weatherDao.getWeather(WEATHER_ID)
            .map(weatherMapper::map)
            .toIOLoadingResult(::handleError)
    }

    override fun cacheCurrentLocation(weather: Weather): Observable<AppResult<Unit>> {
        return weatherDao.insert(entityMapper.map(weather))
            .andThen(Observable.just(Unit))
            .toIOLoadingResult(::handleError)
    }

    override fun hasCurrentLocation(): Observable<AppResult<Boolean>> {
        return weatherDao.isWeatherExist(WEATHER_ID)
            .map { it == 1 }
            .toObservable()
            .toIOLoadingResult(::handleError)
    }

    private fun handleError(throwable: Throwable): AppError {
        return AppError(ErrorType.DB_ERROR)
    }
}