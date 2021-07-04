package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import javax.inject.Inject

class LocalWeatherMapper @Inject constructor() : Mapper<CurrentWeatherEntity, Weather> {

    override fun map(source: CurrentWeatherEntity): Weather {
        return Weather(
            Coordinates(source.lat,source.lon),
            source.name,
            source.country,
            source.timeZone,
            source.currentTemp,
            source.feelTemp,
            source.minTemp,
            source.maxTemp,
            source.condition,
            source.humidity,
            source.windSpeed,
            source.sunrise,
            source.sunset,
            source.uvIndexValue,
            source.unitSystem,
            source.hourlyForecast,
            source.dailyForecast,
            source.updated
        )
    }
}