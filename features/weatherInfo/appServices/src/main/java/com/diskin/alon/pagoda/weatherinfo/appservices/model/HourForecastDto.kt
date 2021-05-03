package com.diskin.alon.pagoda.weatherinfo.appservices.model

data class HourForecastDto(val hour: Long,
                           val condition: WeatherConditionDto,
                           val temp: Double)
