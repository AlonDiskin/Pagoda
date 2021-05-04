package com.diskin.alon.pagoda.settings.infrastructure

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType.LOCATION_BACKGROUND_PERMISSION
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertNotificationSchedulerImpl
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertNotificationSchedulerImpl.Companion.WEATHER_ALERT_WORK_NAME
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertNotificationSchedulerImpl.Companion.WORK_INTERVAL_MINUTES
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
            RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
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
    fun cancelAlertNotificationWhenScheduledToDisable() {
        // Test case fixture
        every { workManager.cancelUniqueWork(any()) } returns mockk()

        // Given

        // When
        scheduler.schedule(AlertInfo(false)).test()

        // Then
        verify { workManager.cancelUniqueWork(WEATHER_ALERT_WORK_NAME) }
    }

    @Test
    fun returnErrorWhenScheduledWithoutLocationPermission() {
        // Test case fixture
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            app, Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED

        // Given

        // When
        val observer = scheduler.schedule(AlertInfo(true)).test()

        // Then
        observer.assertValue(AppResult.Error(AppError(LOCATION_BACKGROUND_PERMISSION)))
    }

    @Test
    fun scheduleAlertNotificationWhenScheduledWithLocationPermission() {
        // Test case fixture
        val workRequestSlot = slot<PeriodicWorkRequest>()

        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            app, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every { workManager.enqueueUniquePeriodicWork(any(),any(),capture(workRequestSlot)) } returns mockk()

        // Given

        // When
        scheduler.schedule(AlertInfo(true)).test()

        // Then
        assertThat(workRequestSlot.captured.workSpec.constraints.requiredNetworkType)
            .isEqualTo(NetworkType.CONNECTED)
        assertThat(workRequestSlot.captured.workSpec.isPeriodic).isTrue()
        assertThat(workRequestSlot.captured.workSpec.intervalDuration)
            .isEqualTo(TimeUnit.MINUTES.toMillis(WORK_INTERVAL_MINUTES))
    }
}