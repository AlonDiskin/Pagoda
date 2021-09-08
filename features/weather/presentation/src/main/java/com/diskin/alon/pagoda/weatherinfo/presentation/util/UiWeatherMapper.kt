package com.diskin.alon.pagoda.weatherinfo.presentation.util

import android.app.Application
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.mapAppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDescriptionDto.*
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiDayForecast
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiHourForecast
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UiWeatherMapper @Inject constructor(
    private val app: Application
) : Mapper<Observable<AppResult<WeatherDto>>,Observable<AppResult<UiWeather>>> {

    override fun map(source: Observable<AppResult<WeatherDto>>): Observable<AppResult<UiWeather>> {
        return source.mapAppResult {
            UiWeather(
                it.name,
                it.timeZone,
                mapLocationTime24HourFormat(it.timeFormat),
                mapLocationTime12HourFormat(it.timeFormat),
                mapTemperature(it.currentTemp),
                mapCondition(it.condition),
                mapConditionIcon(it.condition),
                mapFeelTemperature(it.feelTemp),
                mapMinMaxTemperatures(it.minTemp,it.maxTemp),
                mapHumidity(it.humidity),
                mapWindSpeed(it.windSpeed,it.windSpeedUnitSystem),
                mapHourStamp(it.sunrise,it.timeFormat),
                mapHourStamp(it.sunset,it.timeFormat),
                mapUvIndex(it.uvIndex),
                mapHourForecast(it.hourlyForecast,it.timeFormat),
                mapDayForecast(it.dailyForecast),
                mapLastUpdateTime(it.updated,it.timeFormat),
                mapCurrentTempUnit(it.tempUnitSystem)
            )
        }
    }

    private fun mapCurrentTempUnit(unitSystemDto: UnitSystemDto): String {
        return when(unitSystemDto) {
            UnitSystemDto.METRIC -> "C"
            else -> "F"
        }
    }

    private fun mapLocationTime24HourFormat(timeFormat: TimeFormatDto): String? {
        return when(timeFormat) {
            TimeFormatDto.HOUR_24 -> app.getString(R.string.clock_time_format_24)
            TimeFormatDto.HOUR_12 -> null
        }
    }

    private fun mapLocationTime12HourFormat(timeFormat: TimeFormatDto): String? {
        return when(timeFormat) {
            TimeFormatDto.HOUR_24 -> null
            TimeFormatDto.HOUR_12 -> app.getString(R.string.clock_time_format_12)
        }
    }

    private fun mapLastUpdateTime(updated: Long, timeFormat: TimeFormatDto): String {
        val format =  when(timeFormat) {
            TimeFormatDto.HOUR_24 -> app.getString(R.string.update_time_format_24)
            TimeFormatDto.HOUR_12 -> app.getString(R.string.update_time_format_12)
        }

        val time = SimpleDateFormat(format).format(Date(updated))
        return app.getString(R.string.last_weather_update,time)
    }

    private fun mapTemperature(temp: Double): String {
        return app.getString(R.string.temp,temp.toInt())
    }

    private fun mapFeelTemperature(temp: Double): String {
        return app.getString(R.string.feel_temp,temp.toInt())
    }

    private fun mapMinMaxTemperatures(minTemp: Double,maxTemp: Double): String {
        return app.getString(R.string.min_max_temp,minTemp.toInt(),maxTemp.toInt())
    }

    private fun mapHumidity(humidity: Double): String {
        return humidity.toInt().toString().plus("%")
    }

    private fun mapCondition(condition: WeatherConditionDto): String {
        return condition.description.name
    }

    private fun mapConditionIcon(condition: WeatherConditionDto): Int {
        return when(condition.description) {
            Thunderstorm -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_thunder_day_96
                    else -> R.drawable.ic_anim_weather_thunder_night_96
                }
            }
            Rain, Drizzle -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_rain_day_96
                    else -> R.drawable.ic_anim_weather_rain_night_96
                }
            }
            Snow -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_snow_day_96
                    else -> R.drawable.ic_anim_weather_snow_night_96
                }
            }
            Mist, Fog -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_fog_day_96
                    else -> R.drawable.ic_anim_weather_fog_night_96
                }
            }
            Clear -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_clear_day_96
                    else -> R.drawable.ic_anim_weather_clear_night_96
                }
            }
            Clouds -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_clouds_day_96
                    else -> R.drawable.ic_anim_weather_clouds_night_96
                }
            }
            Haze, Dust, Sand -> {
                when(condition.isDay) {
                    true -> R.drawable.ic_anim_weather_haze_day_96
                    else -> R.drawable.ic_anim_weather_haze_night_96
                }
            }
            Tornado -> R.drawable.ic_anim_weather_tornado_96
            Unknown -> R.drawable.ic_anim_weather_unknown_96
        }
    }

    private fun mapWindSpeed(windSpeed: Double,windUnit: UnitSystemDto): String {
        return when(windUnit) {
            UnitSystemDto.IMPERIAL -> app.getString(R.string.wind_speed_imperial,windSpeed.toInt())
            UnitSystemDto.METRIC -> app.getString(R.string.wind_speed_metric,windSpeed.toInt())
        }
    }

    private fun mapHourStamp(stamp: Long, format: TimeFormatDto): String {
        return when(format) {
            TimeFormatDto.HOUR_12 -> {
                val format = SimpleDateFormat(app.getString(R.string.time_format_12))
                format.format(Date(stamp))
            }

            TimeFormatDto.HOUR_24 -> {
                val format = SimpleDateFormat(app.getString(R.string.time_format_24))
                format.format(Date(stamp))
            }
        }
    }

    private fun mapUvIndex(uvIndex: UvIndexDto): String {
        return when(uvIndex) {
            UvIndexDto.LOW -> app.getString(R.string.uv_index_low)
            UvIndexDto.MODERATE -> app.getString(R.string.uv_index_moderate)
            UvIndexDto.HIGH -> app.getString(R.string.uv_index_high)
            UvIndexDto.VERY_HIGH -> app.getString(R.string.uv_index_very_high)
        }
    }

    private fun mapHourForecast(forecast: List<HourForecastDto>,format: TimeFormatDto):List<UiHourForecast> {
        return forecast.map {
            UiHourForecast(
                mapHourStamp(it.hour,format),
                mapCondition(it.condition),
                mapConditionIcon(it.condition),
                mapTemperature(it.temp)
            )
        }
    }

    private fun mapDayForecast(forecast: List<DayForecastDto>):List<UiDayForecast> {
        return forecast.map {
            UiDayForecast(
                mapDay(it.dayOfWeek),
                mapCondition(it.condition),
                mapConditionIcon(it.condition),
                mapTemperature(it.minTemp),
                mapTemperature(it.maxTemp)
            )
        }
    }

    private fun mapDay(day: Int): String {
        return when(day) {
            1 -> app.getString(R.string.week_day_sunday)
            2 -> app.getString(R.string.week_day_monday)
            3 -> app.getString(R.string.week_day_tuesday)
            4 -> app.getString(R.string.week_day_wednesday)
            5 -> app.getString(R.string.week_day_thursday)
            6 -> app.getString(R.string.week_day_friday)
            7 -> app.getString(R.string.week_day_saturday)
            else -> throw IllegalArgumentException("Wrong day of week arg:$day")
        }
    }
}