package com.diskin.alon.pagoda.weather.infrastructure

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.ListenableWorker.Result
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [WeatherAlertWorker] unit test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class WeatherAlertWorkerTest {

    // Test subject
    private lateinit var worker: WeatherAlertWorker

    // Collaborators
    private val alertProvider: WeatherAlertProvider = mockk()

    @Before
    fun setUp() {
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return WeatherAlertWorker(
                    appContext,
                    workerParameters,
                    alertProvider
                )
            }
        }
        worker = TestListenableWorkerBuilder<WeatherAlertWorker>(
            context = ApplicationProvider.getApplicationContext())
            .setWorkerFactory(workerFactory)
            .build()
    }

    @Test
    fun createNotification_WhenWeatherAlertExistForLocation() {
        // Test case fixture
        val alertRes = WeatherAlert("snow storm")

        every { alertProvider.get() } returns Single.just(alertRes)

        // Given

        // When
        val observer = worker.createWork().test()

        // Then
        verify { alertProvider.get() }

        // And
        val channel = NotificationManagerCompat.from(ApplicationProvider.getApplicationContext())
            .getNotificationChannel(WeatherAlertWorker.CHANNEL_ID)!!
        val notificationManager: NotificationManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications

        assertThat(channel.id).isEqualTo(WeatherAlertWorker.CHANNEL_ID)
        assertThat(channel.name).isEqualTo(WeatherAlertWorker.CHANNEL_NAME)
        assertThat(channel.description).isEqualTo(WeatherAlertWorker.CHANNEL_DESCRIPTION)
        assertThat(notifications.size).isEqualTo(1)
        assertThat(notifications[0].notification.smallIcon.resId).isEqualTo(R.drawable.ic_notification)
        assertThat(notifications[0].notification.extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.notification_title))
        assertThat(notifications[0].notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.notification_text,alertRes.alertMessage))
        observer.assertValue(Result.success())
    }

    @Test
    fun doNotCreateNotification_WhenWeatherAlertNotExistForLocation() {
        // Test case fixture
        val alertRes = WeatherAlert("")

        every { alertProvider.get() } returns Single.just(alertRes)

        // Given

        // When
        val observer = worker.createWork().test()

        // Then
        verify { alertProvider.get() }

        // And
        val notificationManager: NotificationManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications

        assertThat(notifications.size).isEqualTo(0)
        observer.assertValue(Result.success())
    }

    @Test
    fun returnFailResult_WhenWorkError() {
        // Test case fixture
        every { alertProvider.get() } returns Single.error(Throwable())

        // Given

        // When
        val observer = worker.createWork().test()

        // Then
        verify { alertProvider.get() }
        observer.assertValue(Result.failure())
    }
}