package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.CurrentLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.LocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [WeatherViewModel] unit test class.
 */
class WeatherViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: WeatherViewModel

    // Collaborators
    private val model: Model = mockk()

    // Stub data
    private val modelCurrentWeather = BehaviorSubject.create<Result<UiWeather>>()
    private val savedState: SavedStateHandle = SavedStateHandle()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<WeatherModelRequest>()) } returns modelCurrentWeather

        viewModel = WeatherViewModel(model,savedState)
    }

    @Test
    fun setWeatherUpdateAsRefreshWhenCreated() {
        // Given

        // Then
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }

    @Test
    fun subscribeToModelCurrentLocationWeatherUpdatesWhenCreatedWithoutCoordinates() {
        // Given

        // Then
        verify { model.execute(CurrentLocationWeatherModelRequest) }
    }

    @Test
    fun subscribeToModelLocationWeatherUpdatesWhenCreatedWithCoordinates() {
        // Given
        val lat = 89.34
        val lon = 12.45
        savedState.set(LOCATION_LAT,lat)
        savedState.set(LOCATION_LON,lon)
        viewModel = WeatherViewModel(model, savedState)

        // Then
        verify { model.execute(LocationWeatherModelRequest(lat, lon)) }
    }

    @Test
    fun updateViewWhenModelWeatherUpdated() {
        // Given

        // When
        val weather = mockk<UiWeather>()
        modelCurrentWeather.onNext(Result.Success(weather))

        // Then
        assertThat(viewModel.weather.value).isEqualTo(weather)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun refreshModelWeatherDataWhenRefreshed() {
        // Given

        // When
        viewModel.refresh()

        // Then
        verify(exactly = 2) { model.execute(any<WeatherModelRequest>()) }

        // And
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }
}