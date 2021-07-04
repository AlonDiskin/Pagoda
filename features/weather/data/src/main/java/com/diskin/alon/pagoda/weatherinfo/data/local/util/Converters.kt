package com.diskin.alon.pagoda.weatherinfo.data.local.util

import androidx.room.TypeConverter
import com.diskin.alon.pagoda.weatherinfo.domain.*
import com.google.gson.Gson

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun weatherConditionToString(condition: WeatherCondition): String {
        return gson.toJson(condition).toString()
    }

    @TypeConverter
    fun stringToWeatherCondition(str: String): WeatherCondition {
        return gson.fromJson(str,WeatherCondition::class.java)
    }

    @TypeConverter
    fun unitSystemToString(unitSystem: UnitSystem): String {
        return unitSystem.name
    }

    @TypeConverter
    fun stringToUnitSystem(str: String): UnitSystem {
        return UnitSystem.valueOf(str)
    }

    @TypeConverter
    fun hourForecastListToString(list: List<HourForecast>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun stringToHourForecastList(str: String): List<HourForecast> {
        return gson.fromJson(str, Array<HourForecast>::class.java).toList()
    }

    @TypeConverter
    fun dayForecastListToString(list: List<DayForecast>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun stringToDayForecastList(str: String): List<DayForecast> {
        return gson.fromJson(str, Array<DayForecast>::class.java).toList()
    }
}