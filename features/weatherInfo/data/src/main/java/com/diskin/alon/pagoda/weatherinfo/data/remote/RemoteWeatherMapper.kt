package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.domain.*
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import javax.inject.Inject

class RemoteWeatherMapper @Inject constructor() :
    Mapper2<ApiWeatherResponse, ApiLocationResponse, LocationWeather> {

    override fun map(source1: ApiWeatherResponse, source2: ApiLocationResponse): LocationWeather {
        return LocationWeather(
            Coordinates(source1.lat, source1.lon),
            source2.name,
            source2.country,
            source1.timezone,
            source1.current.temp,
            source1.current.feels_like,
            source1.daily[0].temp.min,
            source1.daily[0].temp.max,
            WeatherCondition(
                mapConditionCode(source1.current.weather[0].id),
                mapIsDay(source1.current.weather[0].icon)
            ),
            source1.current.humidity,
            source1.current.wind_speed,
            mapTimeStamp(source1.current.sunrise, source1.timezone).toDate().time,
            mapTimeStamp(source1.current.sunset, source1.timezone).toDate().time,
            source1.current.uvi,
            UnitSystem.METRIC,
            source1.hourly
                .take(12)
                .map {
                    HourForecast(
                        mapTimeStamp(it.dt, source1.timezone).toDate().time,
                        WeatherCondition(
                            mapConditionCode(it.weather[0].id),
                            mapIsDay(it.weather[0].icon)
                        ),
                        it.temp
                    )
                },
            source1.daily
                .map {
                DayForecast(
                    when (mapTimeStamp(it.dt, source1.timezone).dayOfWeek) {
                        DateTimeConstants.SUNDAY -> 1
                        else -> LocalDate(it.dt * 1000).dayOfWeek + 1
                    },
                    it.temp.day,
                    WeatherCondition(
                        mapConditionCode(it.weather[0].id),
                        mapIsDay(it.weather[0].icon)
                    ),
                    it.temp.min,
                    it.temp.max
                )
            }
        )
    }

    private fun mapConditionCode(code: Int): WeatherDescription {
        return when {
            (code in 200..299) -> WeatherDescription.Thunderstorm
            (code in 300..399) -> WeatherDescription.Drizzle
            (code in 500..599) -> WeatherDescription.Rain
            (code in 600..699) -> WeatherDescription.Snow
            (code == 800) -> WeatherDescription.Clear
            (code in 801..804) -> WeatherDescription.Clouds
            (code == 701) -> WeatherDescription.Mist
            (code == 721) -> WeatherDescription.Haze
            (code == 731) -> WeatherDescription.Dust
            (code == 741) -> WeatherDescription.Fog
            (code == 751) -> WeatherDescription.Sand
            (code == 781) -> WeatherDescription.Tornado
            else -> WeatherDescription.Unknown
        }
    }

    private fun mapIsDay(iconName: String): Boolean {
        return iconName.contains('d')
    }

    private fun mapTimeStamp(stamp: Long,timeZone: String): LocalDateTime {
        return LocalDateTime(stamp * 1000, DateTimeZone.forID(timeZone))
    }
}