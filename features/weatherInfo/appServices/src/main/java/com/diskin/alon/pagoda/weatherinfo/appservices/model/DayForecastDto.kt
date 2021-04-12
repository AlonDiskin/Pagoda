package com.diskin.alon.pagoda.weatherinfo.appservices.model

data class DayForecastDto(val dayOfWeek: Int,
                          val condition: WeatherConditionDto,
                          val minTemp: Double,
                          val maxTemp: Double)
