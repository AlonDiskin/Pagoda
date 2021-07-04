package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.util.CurrentWeatherEntityMapper
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * [CurrentWeatherEntityMapper] unit test class.
 */
class CurrentWeatherEntityMapperTest {

    // Test subject
    private lateinit var mapper: CurrentWeatherEntityMapper

    @Before
    fun setUp() {
        mapper = CurrentWeatherEntityMapper()
    }


    @Test
    fun mapWeatherToCurrentWeatherEntity() {
        // Test case fixture
        val updateTime = 12345L
        val calendar: Calendar = mockk()

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns updateTime

        // Given

        // When
        val weather = createWeather()
        val expected =  CurrentWeatherEntity(
            weather.id.lat,
            weather.id.lon,
            weather.name,
            weather.country,
            weather.timeZone,
            weather.currentTemp,
            weather.feelTemp,
            weather.minTemp,
            weather.maxTemp,
            weather.humidity,
            weather.windSpeed,
            weather.sunrise,
            weather.sunset,
            weather.uvIndexValue,
            weather.condition,
            weather.unitSystem,
            weather.hourlyForecast,
            weather.dailyForecast,
            updateTime,
            id = CurrentWeatherEntity.WEATHER_ID
        )
        val actual = mapper.map(weather)

        // Then
        Truth.assertThat(actual).isEqualTo(expected)
    }
}