package com.diskin.alon.pagoda.weather.infrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.shared.AppDataPublisher
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import com.diskin.alon.pagoda.weatherinfo.appservices.model.AlertInfoDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.ScheduleWeatherAlertNotificationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.ScheduleWeatherAlertNotificationUseCase
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

/**
 * [WeatherAlertSettingHandlerService] unit test class.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class,sdk = [28])
class WeatherAlertSettingHandlerServiceTest {

    // Hilt test rule
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var service: WeatherAlertSettingHandlerService

    // Collaborators
    @BindValue @JvmField
    val schedulingUseCase: ScheduleWeatherAlertNotificationUseCase = mockk()
    @BindValue @JvmField
    val alertProvider: AppDataProvider<Observable<WeatherAlertEnabled>> = mockk()
    @BindValue @JvmField
    val errorPublisher: AppDataPublisher<AlertSchedulingError> = mockk()

    // Stub data
    private val alertSubject = BehaviorSubject.create<WeatherAlertEnabled>()

    @Before
    fun setUp() {
        // Stub mock collaborators
        every { alertProvider.get() } returns alertSubject

        // Start service under test
        service = Robolectric.setupService(WeatherAlertSettingHandlerService::class.java)
    }

    @Test
    fun subscribeToAlertEnabling_WhenCreated() {
        // Given

        // Then
        alertSubject.test().assertSubscribed()
    }

    @Test
    fun unsubscribeFromAlertEnabling_WhenDestroyed() {
        // Given

        // When
        service.onDestroy()

        // Then
        assertThat(alertSubject.hasObservers()).isFalse()
    }

    @Test
    fun scheduleAlert_WhenAlertEnabled() {
        // Given
        every { schedulingUseCase.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        alertSubject.onNext(WeatherAlertEnabled(true))
        alertSubject.onNext(WeatherAlertEnabled(false))

        // Then
        verify { schedulingUseCase.execute(ScheduleWeatherAlertNotificationRequest(AlertInfoDto(true))) }
        verify { schedulingUseCase.execute(ScheduleWeatherAlertNotificationRequest(AlertInfoDto(false))) }
    }

    @Test
    fun publishAlertErrorToApp_WhenSchedulingFail() {
        // Given
        val error: AppError = mockk()

        every { schedulingUseCase.execute(any()) } returns Single.just(AppResult.Error(error))
        every { errorPublisher.publish(any()) } returns Unit

        // When
        alertSubject.onNext(WeatherAlertEnabled(true))

        // Then
        verify { errorPublisher.publish(AlertSchedulingError(error)) }
    }
}