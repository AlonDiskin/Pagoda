package com.diskin.alon.pagoda.settings.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.controller.ThemeManager
import com.diskin.alon.pagoda.settings.presentation.viewmodel.SettingsViewModel
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
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
    private val alertErrorProvider: AppDataProvider<Observable<AlertSchedulingError>> = mockk()
    private val themeManager: ThemeManager = mockk()

    // Stub data
    private val alertErrorSubject = BehaviorSubject.create<AlertSchedulingError>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { alertErrorProvider.get() } returns alertErrorSubject

        viewModel = SettingsViewModel(alertErrorProvider, themeManager)
    }

    @Test
    fun configAppDarkMode_whenEnabled() {
        // Given
        every { themeManager.enableDarkMode(any()) } returns Unit
        val enableDarkMode = true

        // When
        viewModel.enableDarkMode(enableDarkMode)

        // Then
        verify { themeManager.enableDarkMode(enableDarkMode) }
    }

    @Test
    fun updateViewError_whenModelAlertSchedulingFail() {
        // Given

        // When
        val appError: AppError = mockk()
        val alertError = AlertSchedulingError(appError)

        alertErrorSubject.onNext(alertError)

        // Then
        Truth.assertThat(viewModel.error.value).isEqualTo(appError)
    }
}