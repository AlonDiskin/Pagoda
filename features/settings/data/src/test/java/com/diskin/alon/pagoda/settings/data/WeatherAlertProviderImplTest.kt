package com.diskin.alon.pagoda.settings.data

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.data.implementation.WeatherAlertProviderImpl
import com.diskin.alon.pagoda.settings.data.local.CurrentLocationProvider
import com.diskin.alon.pagoda.settings.data.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.settings.data.model.UserLocation
import com.diskin.alon.pagoda.settings.data.remote.OpenWeatherMapApi
import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * [WeatherAlertProviderImpl] unit test class.
 */
class WeatherAlertProviderImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var alertProvider: WeatherAlertProviderImpl

    // Collaborators
    private val locationProvider: CurrentLocationProvider = mockk()
    private val api: OpenWeatherMapApi = mockk()
    private val mapper: Mapper<ApiWeatherAlertResponse, WeatherAlert> = mockk()

    @Before
    fun setUp() {
        alertProvider = WeatherAlertProviderImpl(locationProvider,api, mapper)
    }

    @Test
    fun fetchAlertDataWhenQueried() {
        // Test case fixture
        val locationResult = UserLocation(10.45,89.67)
        val apiResponse: ApiWeatherAlertResponse = mockk()
        val mappedAlert: WeatherAlert = mockk()

        every { locationProvider.get() } returns Single.just(locationResult)
        every { api.getWeatherAlert(any(),any()) } returns Single.just(apiResponse)
        every { mapper.map(any()) } returns mappedAlert

        // Given

        // When
        val observer = alertProvider.get().test()

        // Then
        verify { locationProvider.get() }
        verify { api.getWeatherAlert(locationResult.lat, locationResult.lon) }
        observer.assertValue(mappedAlert)
    }
}