package com.diskin.alon.pagoda.weatherinfo.domain

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [LocationWeather] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class LocationWeatherTest {

    @Test
    @Parameters(method = "convertDataParams")
    fun convertToImperialUnits(
        metricWeather: LocationWeather,
        imperialWeather: LocationWeather
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
        metricWeather: LocationWeather,
        imperialWeather: LocationWeather
    ) {
        // Given

        // When
        imperialWeather.toMetric()

        // Then
        assertThat(imperialWeather).isEqualTo(metricWeather)
    }

    private fun convertDataParams() = arrayOf(
        arrayOf(LocationWeather(
            Coordinates(0.0,0.0),
            "name",
            "country",
            "timezone",
            25.0,
            25.0,
            22.0,
            29.0,
            WeatherCondition(WeatherDescription.Clear,true),
            35.0,
            15.0,
            12345L,
            123446L,
            1.2,
            UnitSystem.METRIC,
            emptyList(),
            emptyList()
        ), LocationWeather(
                Coordinates(0.0,0.0),
                "name",
                "country",
                "timezone",
            77.0,
            77.0,
            71.6,
            84.2,
            WeatherCondition(WeatherDescription.Clear,true),
            35.0,
            9.4,
                12345L,
                123446L,
            1.2,
                UnitSystem.IMPERIAL,
                emptyList(),
                emptyList()
            )
        )
    )
}