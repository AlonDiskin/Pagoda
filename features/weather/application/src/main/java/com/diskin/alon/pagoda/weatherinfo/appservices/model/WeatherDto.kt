package com.diskin.alon.pagoda.weatherinfo.appservices.model

data class WeatherDto(val name: String,
                      val country: String,
                      val timeZone: String,
                      val timeFormat: TimeFormatDto,
                      val tempUnitSystem: UnitSystemDto,
                      val windSpeedUnitSystem: UnitSystemDto,
                      val currentTemp: Double,
                      val feelTemp: Double,
                      val minTemp: Double,
                      val maxTemp: Double,
                      val condition: WeatherConditionDto,
                      val humidity: Double,
                      val windSpeed: Double,
                      val sunrise: Long,
                      val sunset: Long,
                      val uvIndex: UvIndexDto,
                      val hourlyForecast: List<HourForecastDto>,
                      val dailyForecast: List<DayForecastDto>,
                      val updated: Long)
