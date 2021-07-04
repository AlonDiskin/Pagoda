package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import java.util.*
import javax.inject.Inject

class CurrentWeatherEntityMapper @Inject constructor() : Mapper<Weather, CurrentWeatherEntity> {

    override fun map(source: Weather): CurrentWeatherEntity {
        return CurrentWeatherEntity(
            source.id.lat,
            source.id.lon,
            source.name,
            source.country,
            source.timeZone,
            source.currentTemp,
            source.feelTemp,
            source.minTemp,
            source.maxTemp,
            source.humidity,
            source.windSpeed,
            source.sunrise,
            source.sunset,
            source.uvIndexValue,
            source.condition,
            source.unitSystem,
            source.hourlyForecast,
            source.dailyForecast,
            Calendar.getInstance().timeInMillis
        )
    }
}