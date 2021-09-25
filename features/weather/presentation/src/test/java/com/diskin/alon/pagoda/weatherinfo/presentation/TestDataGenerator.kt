package com.diskin.alon.pagoda.weatherinfo.presentation

import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiDayForecast
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiHourForecast
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather

fun createTestWeather(): UiWeather {
    return UiWeather(
        "city",
        "America/Chicago",
        "E, dd MMM yyyy HH:mm",
        null,
        "24°",
        "condition",
        10,
        "25°",
        "min max temp",
        "34%",
        "15km/h",
        "06:34",
        "19:34",
        "low",
        listOf(
            UiHourForecast("12:00","condition1",34,"23°"),
            UiHourForecast("13:00","condition2",32,"28°"),
            UiHourForecast("14:00","condition3",35,"32°")
        ),
        listOf(
            UiDayForecast("Sunday","condition4",45,"-10°","32°"),
            UiDayForecast("Monday","condition7",47,"10°","36°")
        ),
        "3 Mar, 12:45",
        "C"
    )
}