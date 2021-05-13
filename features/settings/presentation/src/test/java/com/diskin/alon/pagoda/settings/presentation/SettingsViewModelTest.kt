package com.diskin.alon.pagoda.settings.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [SettingsViewModel] unit test class.
 */
class SettingsViewModelTest {

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
    private lateinit var viewModel: SettingsViewModel

    // Collaborators
    private val model: Model = mockk()

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(model)
    }

    @Test
    fun changeModelWeatherUnitsWhenUnitsChanged() {
        // Test case fixture
        every { model.execute(any<UpdateWeatherUnitModelRequest>()) } returns Unit

        // Given

        // When
        val unit: WeatherUnit = mockk()
        viewModel.changeWeatherUnits(unit)

        // Then
        verify { model.execute(UpdateWeatherUnitModelRequest(unit)) }
    }

    @Test
    fun scheduleModelWeatherAlertWhenAlertEnabled() {
        // Test case fixture
        every { model.execute(any<ScheduleAlertModelRequest>()) } returns SingleSubject.create()

        // Given

        // When
        val enable = true
        viewModel.enableWeatherAlertNotification(enable)

        // Then
        verify { model.execute(ScheduleAlertModelRequest(enable)) }
    }

    @Test
    fun updateErrorViewDataWhenModelWeatherAlertSchedulingFail() {
        // Test case fixture
        val modelScheduling = SingleSubject.create<AppResult<Unit>>()
        every { model.execute(any<ScheduleAlertModelRequest>()) } returns modelScheduling

        // Given

        // When
        viewModel.enableWeatherAlertNotification(false)

        // And
        val error = AppError(ErrorType.LOCATION_BACKGROUND_PERMISSION)
        modelScheduling.onSuccess(AppResult.Error(error))

        // Then
        assertThat(viewModel.error.value).isEqualTo(error)
    }
}