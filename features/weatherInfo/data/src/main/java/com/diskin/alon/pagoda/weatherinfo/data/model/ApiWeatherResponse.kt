package com.diskin.alon.pagoda.weatherinfo.data.model

data class ApiWeatherResponse(val lat: Double,
                              val lon: Double,
                              val timezone: String,
                              val current: Current,
                              val hourly: List<Hourly>,
                              val daily: List<Daily>) {

    data class Current(val sunrise: Long,
                       val sunset: Long,
                       val temp: Double,
                       val feels_like: Double,
                       val humidity: Double,
                       val wind_speed: Double,
                       val uvi: Double,
                       val weather: List<Weather>)

    data class Weather(val id: Int, val icon: String)

    data class Hourly(val dt: Long,val temp: Double,val weather: List<Weather>)

    data class Daily(val dt: Long, val temp: Temp, val weather: List<Weather>)

    data class Temp(val day: Double,
                    val min: Double,
                    val max: Double)
}