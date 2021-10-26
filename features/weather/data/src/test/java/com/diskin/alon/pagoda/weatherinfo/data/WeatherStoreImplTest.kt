package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * [WeatherStoreImpl] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
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
    fun loadWeatherFromRemoteApi_WhenQueried() {
        // Given
        val apiWeatherResponse: ApiWeatherResponse = mockk()
        val apiLocationResponse: ApiLocationResponse = mockk()
        val mappedWeather: Weather = mockk()

        every { api.getCurrentWeather(any(),any()) } returns Single.just(apiWeatherResponse)
        every { api.getLocationDetail(any(),any()) } returns Single.just(listOf(apiLocationResponse))
        every { mapper.map(any(),any()) } returns mappedWeather

        // When
        val lat = 45.789
        val lon = 23.456
        val observer = store.getWeather(lat, lon).test()

        // Then
        verify { api.getCurrentWeather(lat, lon) }
        verify { mapper.map(apiWeatherResponse,apiLocationResponse) }
        observer.assertValue(Result.Success(mappedWeather))
    }

    @Test
    fun handleError_WhenRemoteWeatherLoadFail() {
        // Given
        val apiWeatherSubject = SingleSubject.create<ApiWeatherResponse>()
        val apiLocationSubject = SingleSubject.create<List<ApiLocationResponse>>()
        val appError: AppError = mockk()

        every { api.getCurrentWeather(any(),any()) } returns apiWeatherSubject
        every { api.getLocationDetail(any(),any()) } returns apiLocationSubject
        every { errorHandler.handle(any()) } returns appError

        // When
        val lat = 45.789
        val lon = 23.456
        val observer = store.getWeather(lat, lon).test()

        // Then
        verify { api.getCurrentWeather(lat, lon) }

        // When
        val error = Throwable()
        apiWeatherSubject.onError(error)

        // Then
        verify { errorHandler.handle(error) }
        observer.assertValue(Result.Error(appError))
    }

    @Test
    @Parameters(method = "updateParams")
    fun resolveIsUpdateAvailable(lastUpdate: Long,current: Long,isUpdateAvailable: Boolean) {
        // Given
        val calendar = mockk<Calendar>()

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns current

        // When
        val actual = store.isUpdateAvailable(lastUpdate)

        // Then
        assertThat(actual).isEqualTo(isUpdateAvailable)
    }

    private fun updateParams() = arrayOf(
        arrayOf(
            LocalDateTime(2021,10,12,10,5).toDate().time,
            LocalDateTime(2021,10,12,10,45).toDate().time,
            false
        ),
        arrayOf(
            LocalDateTime(2021,10,12,10,5).toDate().time,
            LocalDateTime(2021,10,12,10,59,59).toDate().time,
            false
        ),
        arrayOf(
            LocalDateTime(2021,10,12,10,5).toDate().time,
            LocalDateTime(2021,10,12,11,0).toDate().time,
            true
        ),
        arrayOf(
            LocalDateTime(2021,10,12,10,5).toDate().time,
            LocalDateTime(2021,10,13,10,5).toDate().time,
            true
        )
    )
}