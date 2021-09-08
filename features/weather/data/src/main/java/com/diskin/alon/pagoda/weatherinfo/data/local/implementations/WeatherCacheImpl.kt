package com.diskin.alon.pagoda.weatherinfo.data.local.implementations

import com.diskin.alon.pagoda.common.appservices.*
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity.Companion.WEATHER_ID
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WeatherCacheImpl @Inject constructor(
    private val weatherDao: CurrentWeatherDao,
    private val weatherMapper: Mapper<CurrentWeatherEntity,Weather>,
    private val entityMapper: Mapper<Weather,CurrentWeatherEntity>
) : WeatherCache {

    override fun getCurrentLocationWeather(): Observable<Result<Weather>> {
        return weatherDao.getWeather(WEATHER_ID)
            .map(weatherMapper::map)
            .toResult(::handleError)
    }

    override fun cacheCurrentLocation(weather: Weather): Single<Result<Unit>> {
        return weatherDao.insert(entityMapper.map(weather))
            .subscribeOn(Schedulers.io())
            .toSingle { }
            .toSingleResult(::handleError)
    }

    override fun hasCurrentLocation(): Single<Result<Boolean>> {
        return weatherDao.isWeatherExist(WEATHER_ID)
            .subscribeOn(Schedulers.io())
            .map { it == 1 }
            .toSingleResult(::handleError)
    }

    private fun handleError(throwable: Throwable): AppError {
        return AppError(ErrorType.DB_ERROR)
    }
}