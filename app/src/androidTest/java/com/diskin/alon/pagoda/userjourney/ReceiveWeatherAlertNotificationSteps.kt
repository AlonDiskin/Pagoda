package com.diskin.alon.pagoda.userjourney

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.*
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.uiautomator.UiSelector
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertNotificationSchedulerImpl
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertWorker
import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.google.common.truth.Truth
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject

/**
 * Step definitions for 'User receive weather alert notification' scenario.
 */
class ReceiveWeatherAlertNotificationSteps(
    server: MockWebServer,
    alertProvider: WeatherAlertProvider
) : GreenCoffeeSteps() {

    private val dispatcher = TestDispatcher()
    private val context = getApplicationContext<Context>()

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)

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
                    alertProvider
                )
            }
        }
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)
    }

    @And("^Open settings screen$")
    fun open_settings_screen() {
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.title_settings))
            .perform(click())
    }

    @And("^Enable weather alert notification$")
    fun enable_weather_alert_notification() {
        onView(withText(R.string.pref_alert_notification_title))
            .perform(click())
        Thread.sleep(1000)
    }

    @When("^Weather alert for user location is received$")
    fun weather_alert_for_user_location_is_received() {
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfosForUniqueWork(
            WeatherAlertNotificationSchedulerImpl.WEATHER_ALERT_WORK_NAME).get()[0]
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setPeriodDelayMet(workInfo.id)
        testDriver.setAllConstraintsMet(workInfo.id)
        Thread.sleep(1000)
    }

    @Then("^App should show alert info in a status bar notification$")
    fun app_should_show_alert_info_in_a_status_bar_notification() {
        val weatherJson = FileUtil.readStringFromFile(dispatcher.weatherAlertRes)
        val alert = JSONObject(weatherJson).getJSONArray("alerts")
            .getJSONObject(0).getString("event")

        DeviceUtil.getDevice().openNotification()
        Truth.assertThat(DeviceUtil.getDevice().findObject(UiSelector().text(alert))).isNotNull()
        DeviceUtil.pressBack()
    }

    private class TestDispatcher() : Dispatcher() {
        val weatherAlertRes = "assets/json/weather_alert.json"
        private val path = "/data/2.5/onecall"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                path -> {
                    if (request.requestUrl.queryParameterNames().contains("lat") &&
                        request.requestUrl.queryParameterNames().contains("lon") &&
                        request.requestUrl.queryParameter("exclude") == "current,minutely,hourly,daily" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(weatherAlertRes))
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