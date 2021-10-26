package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.local.util.Converters
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.domain.DayForecast
import com.diskin.alon.pagoda.weatherinfo.domain.HourForecast
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherCondition
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [Converters] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class ConvertersTest {

    // Test subject
    private lateinit var converters: Converters

    //
    private val gson = Gson()

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    @Parameters(method = "weatherConditionToStringParams")
    fun convertWeatherConditionToString(condition: WeatherCondition,expected: String) {
        // Given

        // When
        val actual = converters.weatherConditionToString(condition)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Parameters(method = "stringToWeatherConditionParams")
    fun convertStringToWeatherCondition(str: String,expected: WeatherCondition) {
        // Given

        // When
        val actual = converters.stringToWeatherCondition(str)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Parameters(method = "hourForecastListToStringParams")
    fun convertHourForecastListToString(list: List<HourForecast>, expected: String) {
        // Given

        // When
        val actual = converters.hourForecastListToString(list)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Parameters(method = "stringToHourForecastListParams")
    fun convertStringToHourForecastList(str: String, expected: List<HourForecast>) {
        // Given

        // When
        val actual = converters.stringToHourForecastList(str)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Parameters(method = "dayForecastListToStringParams")
    fun convertDayForecastListToString(list: List<DayForecast>, expected: String) {
        // Given

        // When
        val actual = converters.dayForecastListToString(list)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Parameters(method = "stringToDayForecastListParams")
    fun convertStringToDayForecastList(str: String, expected: List<DayForecast>) {
        // Given

        // When
        val actual = converters.stringToDayForecastList(str)

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    private fun weatherConditionToStringParams() = arrayOf(
        arrayOf(
            WeatherCondition(WeatherDescription.Clouds,true),
            gson.toJson(WeatherCondition(WeatherDescription.Clouds,true)).toString()
        ),
        arrayOf(
            WeatherCondition(WeatherDescription.Drizzle,false),
            gson.toJson(WeatherCondition(WeatherDescription.Drizzle,false)).toString()
        )
    )

    private fun stringToWeatherConditionParams() = arrayOf(
        arrayOf(
            gson.toJson(WeatherCondition(WeatherDescription.Clouds,true)).toString(),
            WeatherCondition(WeatherDescription.Clouds,true)
        ),
        arrayOf(
            gson.toJson(WeatherCondition(WeatherDescription.Drizzle,false)).toString(),
            WeatherCondition(WeatherDescription.Drizzle,false)
        )
    )

    private fun hourForecastListToStringParams() = arrayOf(
        arrayOf(
            listOf(
                HourForecast(
                    12345L,
                    WeatherCondition(WeatherDescription.Drizzle,false),
                    34.6
                ),
                HourForecast(
                    992345L,
                    WeatherCondition(WeatherDescription.Dust,true),
                    24.2
                )
            ),
            gson.toJson(
                listOf(
                    HourForecast(
                        12345L,
                        WeatherCondition(WeatherDescription.Drizzle,false),
                        34.6
                    ),
                    HourForecast(
                        992345L,
                        WeatherCondition(WeatherDescription.Dust,true),
                        24.2
                    )
                )
            ).toString()
        )
    )

    private fun stringToHourForecastListParams() = arrayOf(
        arrayOf(
            gson.toJson(
                listOf(
                    HourForecast(
                        12345L,
                        WeatherCondition(WeatherDescription.Drizzle,false),
                        34.6
                    ),
                    HourForecast(
                        992345L,
                        WeatherCondition(WeatherDescription.Dust,true),
                        24.2
                    )
                )
            ).toString(),
            listOf(
                HourForecast(
                    12345L,
                    WeatherCondition(WeatherDescription.Drizzle,false),
                    34.6
                ),
                HourForecast(
                    992345L,
                    WeatherCondition(WeatherDescription.Dust,true),
                    24.2
                )
            )
        )
    )

    private fun dayForecastListToStringParams() = arrayOf(
        arrayOf(
            listOf(
                DayForecast(
                    1,
                    32.5,
                    WeatherCondition(WeatherDescription.Drizzle,false),
                    14.6,
                    32.2
                ),
                DayForecast(
                    3,
                    12.5,
                    WeatherCondition(WeatherDescription.Fog,true),
                    14.6,
                    12.2
                )
            ),
            gson.toJson(
                listOf(
                    DayForecast(
                        1,
                        32.5,
                        WeatherCondition(WeatherDescription.Drizzle,false),
                        14.6,
                        32.2
                    ),
                    DayForecast(
                        3,
                        12.5,
                        WeatherCondition(WeatherDescription.Fog,true),
                        14.6,
                        12.2
                    )
                )
            ).toString()
        )
    )

    private fun stringToDayForecastListParams() = arrayOf(
        arrayOf(
            gson.toJson(
                listOf(
                    DayForecast(
                        1,
                        32.5,
                        WeatherCondition(WeatherDescription.Drizzle,false),
                        14.6,
                        32.2
                    ),
                    DayForecast(
                        3,
                        12.5,
                        WeatherCondition(WeatherDescription.Fog,true),
                        14.6,
                        12.2
                    )
                )
            ).toString(),
            listOf(
                DayForecast(
                    1,
                    32.5,
                    WeatherCondition(WeatherDescription.Drizzle,false),
                    14.6,
                    32.2
                ),
                DayForecast(
                    3,
                    12.5,
                    WeatherCondition(WeatherDescription.Fog,true),
                    14.6,
                    12.2
                )
            )
        )
    )
}