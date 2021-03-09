package com.diskin.alon.pagoda.weatherinfo.appservices.model

data class HourForecastDto(val hour: Int,
                           val condition: WeatherConditionDto,
                           val temp: Double)
