package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.data.remote.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
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
 * [RemoteWeatherStoreImpl] unit test class.
 */
class RemoteWeatherStoreImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: RemoteWeatherStoreImpl

    // Collaborators
    private val api: OpenWeatherMapApi = mockk()
    private val errorHandler: NetworkErrorHandler = mockk()
    private val mapper: Mapper2<ApiWeatherResponse, ApiLocationResponse, LocationWeather> = mockk()

    @Before
    fun setUp() {
        store = RemoteWeatherStoreImpl(api, errorHandler, mapper)
    }

    @Test
    fun loadWeatherFromRemoteApiWhenQueried() {
        // Test case fixture
        val apiWeatherResponse: ApiWeatherResponse = mockk()
        val apiLocationResponse: ApiLocationResponse = mockk()
        val mappedWeather: LocationWeather = mockk()

        every { api.getCurrentWeather(any(),any()) } returns Single.just(apiWeatherResponse)
        every { api.getLocationDetail(any(),any()) } returns Single.just(listOf(apiLocationResponse))
        every { mapper.map(any(),any()) } returns mappedWeather

        // Given

        // When store is queried for weather data
        val lat = 45.789
        val lon = 23.456
        val observer = store.get(lat, lon).test()

        // Then store should load weather from api
        verify { api.getCurrentWeather(lat, lon) }

        // And propagate mapped api response to expected result model
        verify { mapper.map(apiWeatherResponse,apiLocationResponse) }
        observer.assertValue(Result.Success(mappedWeather))
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
        val observer = store.get(lat, lon).test()

        // Then store should load weather from api
        verify { api.getCurrentWeather(lat, lon) }

        // When api loading fail
        val error = Throwable()
        apiWeatherSubject.onError(error)

        // Then store should handle api error via error handler
        verify { errorHandler.handle(error) }

        // And propagate expected result model
        observer.assertValue(Result.Error(appError))
    }
}