package com.diskin.alon.pagoda.weatherinfo.domain

import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription.*
import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [Weather] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class LocationWeatherTest {

    @Test
    @Parameters(method = "convertDataParams")
    fun convertToImperialUnits(
        metricWeather: Weather,
        imperialWeather: Weather
    ) {
        // Given

        // When
        metricWeather.toImperial()

        // Then
        assertThat(metricWeather).isEqualTo(imperialWeather)
    }

    @Test
    @Parameters(method = "convertDataParams")
    fun convertToMetricUnits(
        metricWeather: Weather,
        imperialWeather: Weather
    ) {
        // Given

        // When
        imperialWeather.toMetric()

        // Then
        assertThat(imperialWeather).isEqualTo(metricWeather)
    }

    private fun convertDataParams() = arrayOf(
        arrayOf(Weather(
            Coordinates(0.0,0.0),
            "name",
            "country",
            "timezone",
            25.0,
            25.0,
            22.0,
            29.0,
            WeatherCondition(Clear,true),
            35.0,
            15.0,
            12345L,
            123446L,
            1.2,
            UnitSystem.METRIC,
            listOf(HourForecast(12000,WeatherCondition(Clear,true),23.0),
                HourForecast(13000,WeatherCondition(Clear,true),24.0),
                HourForecast(14000,WeatherCondition(Clear,true),25.0),
                HourForecast(15000,WeatherCondition(Clear,true),25.0),
                HourForecast(16000,WeatherCondition(Clear,true),26.0),
                HourForecast(17000,WeatherCondition(Clear,false),25.0),
                HourForecast(18000,WeatherCondition(Clear,false),25.0),
                HourForecast(19000,WeatherCondition(Clouds,false),24.0),
                HourForecast(20000,WeatherCondition(Clouds,false),24.0),
                HourForecast(21000,WeatherCondition(Clouds,false),24.0),
                HourForecast(22000,WeatherCondition(Drizzle,false),21.0),
                HourForecast(23000,WeatherCondition(Drizzle,false),21.0)),
            listOf(DayForecast(1,30.0,WeatherCondition(Clouds,true),28.0,32.0),
                DayForecast(2,29.0,WeatherCondition(Clouds,true),26.0,33.0),
                DayForecast(3,29.0,WeatherCondition(Clouds,true),29.0,32.0),
                DayForecast(4,27.0,WeatherCondition(Rain,true),21.0,30.0),
                DayForecast(5,29.0,WeatherCondition(Thunderstorm,true),26.0,33.0),
                DayForecast(6,29.0,WeatherCondition(Thunderstorm,true),26.0,28.0),
                DayForecast(7,22.0,WeatherCondition(Fog,true),21.0,25.0),
                DayForecast(1,22.0,WeatherCondition(Fog,true),21.0,25.0)),
            34567L
        ), Weather(
                Coordinates(0.0,0.0),
                "name",
                "country",
                "timezone",
            77.0,
            77.0,
            71.6,
            84.2,
            WeatherCondition(Clear,true),
            35.0,
            9.4,
                12345L,
                123446L,
            1.2,
                UnitSystem.IMPERIAL,
            listOf(HourForecast(12000,WeatherCondition(Clear,true),73.4),
                HourForecast(13000,WeatherCondition(Clear,true),75.2),
                HourForecast(14000,WeatherCondition(Clear,true),77.0),
                HourForecast(15000,WeatherCondition(Clear,true),77.0),
                HourForecast(16000,WeatherCondition(Clear,true),78.8),
                HourForecast(17000,WeatherCondition(Clear,false),77.0),
                HourForecast(18000,WeatherCondition(Clear,false),77.0),
                HourForecast(19000,WeatherCondition(Clouds,false),75.2),
                HourForecast(20000,WeatherCondition(Clouds,false),75.2),
                HourForecast(21000,WeatherCondition(Clouds,false),75.2),
                HourForecast(22000,WeatherCondition(Drizzle,false),69.8),
                HourForecast(23000,WeatherCondition(Drizzle,false),69.8)),
            listOf(DayForecast(1,86.0,WeatherCondition(Clouds,true),82.4,89.6),
                DayForecast(2,84.2,WeatherCondition(Clouds,true),78.8,91.4),
                DayForecast(3,84.2,WeatherCondition(Clouds,true),84.2,89.6),
                DayForecast(4,80.6,WeatherCondition(Rain,true),69.8,86.0),
                DayForecast(5,84.2,WeatherCondition(Thunderstorm,true),78.8,91.4),
                DayForecast(6,84.2,WeatherCondition(Thunderstorm,true),78.8,82.4),
                DayForecast(7,71.6,WeatherCondition(Fog,true),69.8,77.0),
                DayForecast(1,71.6,WeatherCondition(Fog,true),69.8,77.0)),
            34567L
            )
        )
    )
}