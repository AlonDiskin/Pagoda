package com.diskin.alon.pagoda.weatherinfo.appservices.model

data class LocationWeatherDto(val name: String,
                              val country: String,
                              val timeZone: String,
                              val currentTemp: Double,
                              val feelTemp: Double,
                              val minTemp: Double,
                              val maxTemp: Double,
                              val condition: WeatherConditionDto,
                              val humidity: Double,
                              val windSpeed: Double,
                              val sunrise: Long,
                              val sunset: Long,
                              val unitSystem: UnitSystemDto,
                              val uvIndex: UvIndexDto,
                              val hourlyForecast: List<HourForecastDto>,
                              val dailyForecast: List<DayForecastDto>)
