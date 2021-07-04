package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * [WeatherStoreImpl] unit test class.
 */
class WeatherStoreImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: WeatherStoreImpl

    // Collaborators
    private val api: OpenWeatherMapApi = mockk()
    private val errorHandler: NetworkErrorHandler = mockk()
    private val mapper: Mapper2<ApiWeatherResponse, ApiLocationResponse, Weather> = mockk()

    @Before
    fun setUp() {
        store = WeatherStoreImpl(api, errorHandler, mapper)
    }

    @Test
    fun loadWeatherFromRemoteApiWhenQueried() {
        // Test case fixture
        val apiWeatherResponse: ApiWeatherResponse = mockk()
        val apiLocationResponse: ApiLocationResponse = mockk()
        val mappedWeather: Weather = mockk()

        every { api.getCurrentWeather(any(),any()) } returns Single.just(apiWeatherResponse)
        every { api.getLocationDetail(any(),any()) } returns Single.just(listOf(apiLocationResponse))
        every { mapper.map(any(),any()) } returns mappedWeather

        // Given

        // When store is queried for weather data
        val lat = 45.789
        val lon = 23.456
        val observer = store.getWeather(lat, lon).test()

        // Then store should load weather from api
        verify { api.getCurrentWeather(lat, lon) }

        // And propagate mapped api response to expected result model
        verify { mapper.map(apiWeatherResponse,apiLocationResponse) }
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1,AppResult.Success(mappedWeather))
    }

    @Test
    fun handleErrorWhenRemoteWeatherLoadFail() {
        // Test case fixture
        val apiWeatherSubject = SingleSubject.create<ApiWeatherResponse>()
        val apiLocationSubject = SingleSubject.create<List<ApiLocationResponse>>()
        val appError: AppError = mockk()

        every { api.getCurrentWeather(any(),any()) } returns apiWeatherSubject
        every { api.getLocationDetail(any(),any()) } returns apiLocationSubject
        every { errorHandler.handle(any()) } returns appError

        // Given

        // When store is queried for weather data
        val lat = 45.789
        val lon = 23.456
        val observer = store.getWeather(lat, lon).test()

        // Then store should load weather from api
        verify { api.getCurrentWeather(lat, lon) }

        // When api loading fail
        val error = Throwable()
        apiWeatherSubject.onError(error)

        // Then store should handle api error via error handler
        verify { errorHandler.handle(error) }

        // And propagate expected result model
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1,AppResult.Error(appError))
    }
}