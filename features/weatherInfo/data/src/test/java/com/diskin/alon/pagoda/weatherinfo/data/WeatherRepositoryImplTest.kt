package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherStore
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
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
    private val remoteStore: RemoteWeatherStore = mockk()

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(remoteStore)
    }

    @Test
    fun loadLocationWeatherFromRemoteStoreWhenQueried() {
        // Test case fixture
        val remoteRes: Observable<Result<LocationWeather>> = mockk()

        every { remoteStore.get(any(),any()) } returns remoteRes

        // Given

        // When
        val lat = 23.56
        val lon = 56.78
        val actual = repository.get(lat, lon)

        // Then
        verify { remoteStore.get(lat, lon) }
        assertThat(actual).isEqualTo(remoteRes)
    }
}