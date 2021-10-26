package com.diskin.alon.pagoda.weatherinfo.featuretesting.schedule_weather_alert_notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertNotificationSchedulerImpl
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertSettingHandlerService
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertWorker
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.robolectric.Robolectric
import org.robolectric.Shadows

/**
 * Step definitions for 'User enable\disable weather alert notification' scenario.
 */
class EnablingAlertSteps(
    server: MockWebServer,
    locationProvider: UserLocationProvider,
    weatherAlertProvider: WeatherAlertProvider,
    private val alertProvider: AppDataProvider<Observable<WeatherAlertEnabled>>,
) : GreenCoffeeSteps() {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dispatcher = TestDispatcher()
    private val alertEnabledSubject = BehaviorSubject.create<WeatherAlertEnabled>()

    init {
        // Grant needed system permission
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        // Prepare test WorkManager
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return WeatherAlertWorker(
                    appContext,
                    workerParameters,
                    weatherAlertProvider
                )
            }
        }
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        // Set test server
        server.setDispatcher(dispatcher)

        // Set test location provider
        every { locationProvider.getLocation() } returns Single.just(Result.Success(
            UserLocation(dispatcher.locationLat,dispatcher.locationLon)))

        // Set test alert enabled Provider
        every { alertProvider.get() } returns alertEnabledSubject

        // Start alert enabling listener service
        Robolectric.setupService(WeatherAlertSettingHandlerService::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Given("^Weather alert notification is currently \"([^\"]*)\"$")
    fun weather_alert_notification_is_currently(current: String) {
        when(current) {
            "disabled" -> checkAlertNotificationWorkCanceled()
            "enabled" -> createAlertNotificationWork()
            else -> throw IllegalArgumentException("Unknown scenario argument:${current}")
        }
    }

    @When("^User \"([^\"]*)\" notification$")
    fun user_change_notification(userEnable: String) {
        when(userEnable) {
            "enable" -> alertEnabledSubject.onNext(WeatherAlertEnabled(true))
            "disable" -> alertEnabledSubject.onNext(WeatherAlertEnabled(false))
            else -> throw IllegalArgumentException("Unknown scenario argument:${userEnable}")
        }
    }

    @Then("^Show \"([^\"]*)\" alert notification in status bar when alert received$")
    fun show_alert_notification_in_status_bar_when_alert_received(showNotification: String) {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        when(showNotification) {
            "show" -> checkNotificationShown()
            "not show" -> checkNotificationNotShown()
            else -> throw IllegalArgumentException("Unknown scenario argument:${showNotification}")
        }
    }

    private fun checkNotificationNotShown() {
        val notificationManager: NotificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications

        assertThat(notifications.size).isEqualTo(0)
    }

    private fun checkNotificationShown() {
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfosForUniqueWork(
            WeatherAlertNotificationSchedulerImpl.WEATHER_ALERT_WORK_NAME).get()[0]

        // Verify notification work has been scheduled
        assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)

        // Set notification work constrains and delay as met, for work to run
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setPeriodDelayMet(workInfo.id)
        testDriver.setAllConstraintsMet(workInfo.id)

        // Verify alert notification shown
        val notificationManager: NotificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications
        val alertMessage = getServerAlertMessage()

        assertThat(notifications.size).isEqualTo(1)
        assertThat(notifications[0].notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo("Weather alert for your area:${alertMessage}")
    }

    private fun getServerAlertMessage(): String {
        val weatherAlertJson = getJsonFromResource(dispatcher.weatherAlertRes)

        return JSONObject(weatherAlertJson).getJSONArray("alerts")
            .getJSONObject(0).getString("event")
    }

    private fun createAlertNotificationWork() {
        alertEnabledSubject.onNext(WeatherAlertEnabled(true))
    }

    private fun checkAlertNotificationWorkCanceled() {
        val workManager = WorkManager.getInstance(context)
        val workCount = workManager.getWorkInfosForUniqueWork(
            WeatherAlertNotificationSchedulerImpl.WEATHER_ALERT_WORK_NAME).get().size

        assertThat(workCount).isEqualTo(0)
    }

    private class TestDispatcher: Dispatcher() {
        val weatherAlertRes = "json/weather_alert.json"
        val locationLat = 55.7558
        val locationLon = 37.6173
        private val weatherAlertPath = "/data/2.5/onecall"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherAlertPath -> {
                    if (request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                        request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                        request.requestUrl.queryParameter("exclude") == "current,minutely,hourly,daily" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(getJsonFromResource(weatherAlertRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}