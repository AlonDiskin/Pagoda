package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.*
import com.diskin.alon.pagoda.weatherinfo.domain.*
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription.*

fun createUserLocation(): UserLocation = UserLocation(34.567,39.567)

fun createLocationRequest(): LocationRequest = LocationRequest(12.34,56.78)

fun createUseCaseTestParams() = arrayOf(
    LocationWeather(
        Coordinates(34.56,78.90),
        "name",
        "country",
        "timezone",
        23.5,
        25.0,
        20.0,
        26.0,
        WeatherCondition(Clear,true),
        15.0,
        5.5,
        120000L,
        160000L,
        2.0,
        UnitSystem.METRIC,
        listOf(HourForecast(12,WeatherCondition(Clear,true),23.0),
            HourForecast(13,WeatherCondition(Clear,true),24.0),
            HourForecast(14,WeatherCondition(Clear,true),25.0),
            HourForecast(15,WeatherCondition(Clear,true),25.0),
            HourForecast(16,WeatherCondition(Clear,true),26.0),
            HourForecast(17,WeatherCondition(Clear,false),25.0),
            HourForecast(18,WeatherCondition(Clear,false),25.0),
            HourForecast(19,WeatherCondition(Clouds,false),24.0),
            HourForecast(20,WeatherCondition(Clouds,false),24.0),
            HourForecast(21,WeatherCondition(Clouds,false),24.0),
            HourForecast(22,WeatherCondition(Drizzle,false),21.0),
            HourForecast(23,WeatherCondition(Drizzle,false),21.0)),
        listOf(DayForecast(1,30.0,WeatherCondition(Clouds,true),28.0,32.0),
            DayForecast(2,29.0,WeatherCondition(Clouds,true),26.0,33.0),
            DayForecast(3,29.0,WeatherCondition(Clouds,true),29.0,32.0),
            DayForecast(4,27.0,WeatherCondition(Rain,true),21.0,30.0),
            DayForecast(5,29.0,WeatherCondition(Thunderstorm,true),26.0,33.0),
            DayForecast(6,29.0,WeatherCondition(Thunderstorm,true),26.0,28.0),
            DayForecast(7,22.0,WeatherCondition(Fog,true),21.0,25.0),
            DayForecast(1,22.0,WeatherCondition(Fog,true),21.0,25.0)),
    ),
    TemperatureUnitPref(UnitPrefSystem.METRIC),
    WindSpeedUnitPref(UnitPrefSystem.IMPERIAL),
    TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24),
    LocationWeatherDto(
        "name",
        "country",
        "timezone",
        TimeFormatDto.HOUR_24,
        UnitSystemDto.METRIC,
        UnitSystemDto.IMPERIAL,
        23.5,
        25.0,
        20.0,
        26.0,
        WeatherConditionDto(WeatherDescriptionDto.Clear,true),
        15.0,
        3.4,
        120000L,
        160000L,
        UvIndexDto.LOW,
        listOf(HourForecastDto(12,WeatherConditionDto(WeatherDescriptionDto.Clear,true),23.0),
            HourForecastDto(13, WeatherConditionDto(WeatherDescriptionDto.Clear,true),24.0),
            HourForecastDto(14,WeatherConditionDto(WeatherDescriptionDto.Clear,true),25.0),
            HourForecastDto(15,WeatherConditionDto(WeatherDescriptionDto.Clear,true),25.0),
            HourForecastDto(16,WeatherConditionDto(WeatherDescriptionDto.Clear,true),26.0),
            HourForecastDto(17,WeatherConditionDto(WeatherDescriptionDto.Clear,false),25.0),
            HourForecastDto(18,WeatherConditionDto(WeatherDescriptionDto.Clear,false),25.0),
            HourForecastDto(19,WeatherConditionDto(WeatherDescriptionDto.Clouds,false),24.0),
            HourForecastDto(20,WeatherConditionDto(WeatherDescriptionDto.Clouds,false),24.0),
            HourForecastDto(21,WeatherConditionDto(WeatherDescriptionDto.Clouds,false),24.0),
            HourForecastDto(22,WeatherConditionDto(WeatherDescriptionDto.Drizzle,false),21.0),
            HourForecastDto(23,WeatherConditionDto(WeatherDescriptionDto.Drizzle,false),21.0)),
        listOf(DayForecastDto(1,WeatherConditionDto(WeatherDescriptionDto.Clouds,true),28.0,32.0),
            DayForecastDto(2,WeatherConditionDto(WeatherDescriptionDto.Clouds,true),26.0,33.0),
            DayForecastDto(3,WeatherConditionDto(WeatherDescriptionDto.Clouds,true),29.0,32.0),
            DayForecastDto(4,WeatherConditionDto(WeatherDescriptionDto.Rain,true),21.0,30.0),
            DayForecastDto(5,WeatherConditionDto(WeatherDescriptionDto.Thunderstorm,true),26.0,33.0),
            DayForecastDto(6,WeatherConditionDto(WeatherDescriptionDto.Thunderstorm,true),26.0,28.0),
            DayForecastDto(7,WeatherConditionDto(WeatherDescriptionDto.Fog,true),21.0,25.0),
            DayForecastDto(1,WeatherConditionDto(WeatherDescriptionDto.Fog,true),21.0,25.0))
    )
)