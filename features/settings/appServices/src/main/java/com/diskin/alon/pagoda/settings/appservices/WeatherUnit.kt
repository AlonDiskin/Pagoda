package com.diskin.alon.pagoda.settings.appservices

sealed class WeatherUnit {

    enum class HourFormat { HOUR_12,HOUR_24 }

    enum class UnitSystem { METRIC,IMPERIAL }

    data class Temperature(val system: UnitSystem): WeatherUnit()

    data class TimeFormat(val format: HourFormat): WeatherUnit()

    data class WindSpeed(val system: UnitSystem): WeatherUnit()
}