package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.model.CurrentWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.google.common.truth.Truth.*
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
    private val modelCurrentWeather = BehaviorSubject.create<Result<LocationWeatherDto>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<CurrentWeatherModelRequest>()) } returns modelCurrentWeather

        viewModel = WeatherViewModel(model)
    }

    @Test
    fun setWeatherUpdateAsRefreshWhenCreated() {
        // Given

        // Then
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }

    @Test
    fun subscribeToModelWeatherUpdatesWhenCreated() {
        // Given

        // Then
        verify { model.execute(CurrentWeatherModelRequest()) }
    }

    @Test
    fun updateViewWhenModelWeatherUpdated() {
        // Given

        // When
        val weather = mockk<LocationWeatherDto>()
        modelCurrentWeather.onNext(Result.Success(weather))

        // Then
        assertThat(viewModel.weather.value).isEqualTo(weather)
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.EndRefresh)
    }

    @Test
    fun initModelWeatherDataUpdateWhenRefreshed() {
        // Given

        // When
        viewModel.refresh()

        // Then
        verify(exactly = 2) { model.execute(CurrentWeatherModelRequest()) }

        // And
        assertThat(viewModel.update.value).isEqualTo(UpdateViewData.Refresh)
    }
}