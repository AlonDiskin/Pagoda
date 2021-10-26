package com.diskin.alon.pagoda.weather.infrastructure

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertNotificationSchedulerImpl.Companion.WEATHER_ALERT_WORK_NAME
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertNotificationSchedulerImpl.Companion.WORK_INTERVAL_MINUTES
import com.diskin.alon.pagoda.weatherinfo.appservices.model.AlertInfoDto
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * [WeatherAlertNotificationSchedulerImpl] unit test.
 */
class WeatherAlertNotificationSchedulerImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var scheduler: WeatherAlertNotificationSchedulerImpl

    // Collaborators
    private val workManager: WorkManager = mockk()
    private val app: Application = mockk()

    @Before
    fun setUp() {
        scheduler = WeatherAlertNotificationSchedulerImpl(workManager, app)
    }

    @Test
    fun cancelAlertNotification_WhenScheduledToDisable() {
        // Test case fixture
        every { workManager.cancelUniqueWork(any()) } returns mockk()

        // Given

        // When
        scheduler.schedule(AlertInfoDto(false)).test()

        // Then
        verify { workManager.cancelUniqueWork(WEATHER_ALERT_WORK_NAME) }
    }

    @Test
    fun returnError_WhenScheduledWithoutLocationPermission() {
        // Test case fixture
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            app, Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED

        // Given

        // When
        val observer = scheduler.schedule(AlertInfoDto(true)).test()

        // Then
        observer.assertValue(AppResult.Error(AppError(ErrorType.LOCATION_BACKGROUND_PERMISSION)))
    }

    @Test
    fun scheduleAlertNotification_WhenScheduledWithLocationPermission() {
        // Test case fixture
        val workRequestSlot = slot<PeriodicWorkRequest>()

        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            app, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every { workManager.enqueueUniquePeriodicWork(any(),any(),capture(workRequestSlot)) } returns mockk()

        // Given

        // When
        scheduler.schedule(AlertInfoDto(true)).test()

        // Then
        assertThat(workRequestSlot.captured.workSpec.constraints.requiredNetworkType)
            .isEqualTo(NetworkType.CONNECTED)
        assertThat(workRequestSlot.captured.workSpec.isPeriodic).isTrue()
        assertThat(workRequestSlot.captured.workSpec.intervalDuration)
            .isEqualTo(TimeUnit.MINUTES.toMillis(WORK_INTERVAL_MINUTES))
    }
}