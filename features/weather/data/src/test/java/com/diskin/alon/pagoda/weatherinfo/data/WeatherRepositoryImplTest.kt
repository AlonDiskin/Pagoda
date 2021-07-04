package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [WeatherRepositoryImpl] unit test class.
 */
class WeatherRepositoryImplTest {

    // Test subject
    private lateinit var repository: WeatherRepositoryImpl

    // Collaborators
    private val weatherStore: WeatherStore = mockk()
    private val locationProvider: UserLocationProvider = mockk()
    private val cache: WeatherCache = mockk()

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(weatherStore,locationProvider,cache)
    }

    @Test
    fun getWeatherFromRemoteWhenQueriedForWeather() {
        // Test case fixture
        val remoteRes: Observable<AppResult<Weather>> = mockk()

        every { weatherStore.getWeather(any(),any()) } returns remoteRes

        // Given

        // When
        val lat = 23.56
        val lon = 56.78
        val actual = repository.get(lat, lon)

        // Then
        verify { weatherStore.getWeather(lat, lon) }
        assertThat(actual).isEqualTo(remoteRes)
    }

    @Test
    fun getLocalCachedWeatherAndUpdateCacheWhenQueriedForCurrentWeather() {
        // TODO
    }
}