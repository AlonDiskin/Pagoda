package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.domain.*
import io.mockk.mockk

fun createCurrentWeatherEntity() =
    CurrentWeatherEntity(
        23.5,
        45.7,
        "name",
        "country",
        "time_zone",
        12.4,
        15.6,
        5.6,
        16.7,
        23.4,
        3.4,
        23456L,
        6789L,
        3.4,
        WeatherCondition(WeatherDescription.Fog,true),
        UnitSystem.IMPERIAL,
        listOf(
            HourForecast(
                12345L,
                WeatherCondition(WeatherDescription.Drizzle,false),
                34.6
            ),
            HourForecast(
                992345L,
                WeatherCondition(WeatherDescription.Dust,true),
                24.2
            )
        ),
        listOf(
            DayForecast(
                1,
                32.5,
                WeatherCondition(WeatherDescription.Drizzle,false),
                14.6,
                32.2
            ),
            DayForecast(
                3,
                12.5,
                WeatherCondition(WeatherDescription.Fog,true),
                14.6,
                12.2
            )
        ),
        34567L
    )


fun createWeather() =
    Weather(
        Coordinates(23.5, 45.7),
        "name",
        "country",
        "time_zone",
        12.4,
        15.6,
        5.6,
        16.7,
        WeatherCondition(WeatherDescription.Fog,true),
        3.4,
        23.4,
        6789L,
        346789L,
        3.7,
        UnitSystem.IMPERIAL,
        List(12) { mockk() },
        List(8) { mockk() },
        12345L
    )