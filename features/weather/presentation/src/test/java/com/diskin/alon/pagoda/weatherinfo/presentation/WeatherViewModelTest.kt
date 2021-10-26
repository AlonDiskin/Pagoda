package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.model.LocationWeatherModelRequest.UserLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.LocationWeatherModelRequest.WorldLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel.CoordinatesState
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
    private val modelWeather = BehaviorSubject.create<AppResult<UiWeather>>()
    private val savedState: SavedStateHandle = SavedStateHandle()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<ModelRequest<*,*>>()) } returns modelWeather

        // Init subject
        viewModel = WeatherViewModel(model,savedState)
    }

    @Test
    fun requestCurrentWeatherUpdatesFromModel_WhenCreatedWithoutSavedLocationState() {
        // Given

        // Then
        verify { model.execute(UserLocationWeatherModelRequest) }
    }

    @Test
    fun requestWorldLocationWeatherUpdatesFromModel_WhenCreatedWithSavedLocationState() {
        // Given
        val coordinates = CoordinatesState(56.7,78.9)
        savedState[WeatherViewModel.ARG_COORDINATES] = coordinates
        viewModel = WeatherViewModel(model,savedState)

        // Then
        verify { model.execute(
            WorldLocationWeatherModelRequest(
                coordinates.lat,
                coordinates.lon
            )
        ) }
    }

    @Test
    fun updateViewWeather_WhenModelUpdateWeather() {
        // Given
        val weather = mockk<UiWeather>()

        // When
        modelWeather.onNext(AppResult.Success(weather))

        // Then
        assertThat(viewModel.weather.value).isEqualTo(weather)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun updateViewError_WhenModelWeatherErrors() {
        // Given
        val error = mockk<AppError>()

        // When
        modelWeather.onNext(AppResult.Error(error))

        // Then
        assertThat(viewModel.error.value).isEqualTo(error)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun updateViewLoading_WhenModelLoadWeather() {
        // Given

        // When
        modelWeather.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }

    @Test
    fun requestModel_WeatherDataRefreshWhenRefreshed() {
        // Given

        // When
        viewModel.refresh()

        // Then
        verify(exactly = 2) { model.execute(UserLocationWeatherModelRequest) }
    }

    @Test
    fun requestCurrentLocationWeatherFromModel_WhenAskedByClient() {
        // Given

        // When
        viewModel.loadCurrentLocationWeather()

        // Then
        verify(exactly = 2) { model.execute(UserLocationWeatherModelRequest) }
    }

    @Test
    fun requestWorldLocationWeatherFromModel_WhenAskedByClient() {
        // Given
        val lat = 67.8
        val lon = 34.8

        // When
        viewModel.loadLocationWeather(lat, lon)

        // Then
        verify { model.execute(
            WorldLocationWeatherModelRequest(
                lat,
                lon
            )
        ) }
    }

    @Test
    fun saveCoordinatesState_WhenAskedToLoadWorldLocationWeather() {
        // Given
        val lat = 67.8
        val lon = 34.8

        // When
        viewModel.loadLocationWeather(lat, lon)

        // Then
        val coordinates: CoordinatesState? = savedState[WeatherViewModel.ARG_COORDINATES]
        assertThat(coordinates).isEqualTo(CoordinatesState(lat, lon))
    }

    @Test
    fun nullifyCoordinatesState_WhenAskedToLoadCurrentLocationWeather() {
        // Given

        // When
        viewModel.loadCurrentLocationWeather()

        // Then
        val coordinates: CoordinatesState? = savedState[WeatherViewModel.ARG_COORDINATES]
        assertThat(coordinates).isNull()
    }
}