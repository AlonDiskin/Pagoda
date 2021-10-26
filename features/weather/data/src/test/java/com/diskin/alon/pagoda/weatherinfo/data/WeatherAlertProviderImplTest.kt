package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherAlertProviderImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
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
    private val locationProvider: UserLocationProvider = mockk()
    private val api: OpenWeatherMapApi = mockk()
    private val mapper: Mapper<ApiWeatherAlertResponse, WeatherAlert> = mockk()

    @Before
    fun setUp() {
        alertProvider = WeatherAlertProviderImpl(locationProvider,api, mapper)
    }

    @Test
    fun loadAlertData_WhenQueried() {
        // Given
        val locationResult = UserLocation(10.45,89.67)
        val apiResponse: ApiWeatherAlertResponse = mockk()
        val mappedAlert: WeatherAlert = mockk()

        every { locationProvider.getLocation() } returns Single.just(Result.Success(locationResult))
        every { api.getWeatherAlert(locationResult.lat, locationResult.lon) } returns Single.just(apiResponse)
        every { mapper.map(apiResponse) } returns mappedAlert

        // When
        val observer = alertProvider.get().test()

        // Then
        verify { locationProvider.getLocation() }
        verify { api.getWeatherAlert(locationResult.lat, locationResult.lon) }
        verify { mapper.map(apiResponse) }
        observer.assertValue(mappedAlert)
    }
}