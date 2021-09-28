package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UserLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WorldLocationWeatherModelRequest
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
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * [WeatherViewModel] unit test class.
 */
@RunWith(Parameterized::class)
class WeatherViewModelTest(
    private val lat: Float?,
    private val lon: Float?,
    private val request: ModelRequest<*,*>
) {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }

        @JvmStatic
        @Parameterized.Parameters()
        fun data() = listOf(
            arrayOf(23.4F, 45.7F,WorldLocationWeatherModelRequest(23.4, 45.7)),
            arrayOf<Any?>(null,null,UserLocationWeatherModelRequest)
        )
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
        every { model.execute(any<WorldLocationWeatherModelRequest>()) } returns modelWeather
        every { model.execute(any<UserLocationWeatherModelRequest>()) } returns modelWeather

        // Init subject
        lat?.let { savedState.set(ARG_LAT,lat) }
        lon?.let { savedState.set(ARG_LON,lon) }
        viewModel = WeatherViewModel(model,savedState)
    }

    @Test
    fun requestWeatherUpdateFromModelWhenCreated() {
        // Given

        // Then
        verify { model.execute(request) }
    }

    @Test
    fun updateViewWeatherWhenModelUpdateWeather() {
        // Given
        val weather = mockk<UiWeather>()

        // When
        modelWeather.onNext(AppResult.Success(weather))

        // Then
        assertThat(viewModel.weather.value).isEqualTo(weather)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun updateViewErrorWhenModelWeatherErrors() {
        // Given
        val error = mockk<AppError>()

        // When
        modelWeather.onNext(AppResult.Error(error))

        // Then
        assertThat(viewModel.error.value).isEqualTo(error)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun updateViewLoadingWhenModelLoadWeather() {
        // Given

        // When
        modelWeather.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }

    @Test
    fun requestModelWeatherDataRefreshWhenRefreshed() {
        // Given

        // When
        viewModel.refresh()

        // Then
        verify(exactly = 2) { model.execute(request) }
    }
}