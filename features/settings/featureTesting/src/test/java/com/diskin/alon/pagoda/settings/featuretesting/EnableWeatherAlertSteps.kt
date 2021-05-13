package com.diskin.alon.pagoda.settings.featuretesting

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.preference.get
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertNotificationSchedulerImpl
import com.diskin.alon.pagoda.settings.infrastructure.implementation.WeatherAlertWorker
import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import com.diskin.alon.pagoda.settings.presentation.SettingsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import org.robolectric.Shadows

/**
 * Step definitions for 'User enable alert notification' scenario.
 */
class EnableWeatherAlertSteps(
    weatherAlertProvider: WeatherAlertProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val context = getApplicationContext<Context>()
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefKey = context.getString(R.string.pref_alert_notification_key)
    private val alert = WeatherAlert("Snow storm")

    init {
        // Grant needed system permission
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        // Prepare weather alert provider
        every { weatherAlertProvider.get() } returns Single.just(alert)

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
    }

    @Given("^Weather alert notification preference han not been modified$")
    fun weather_alert_notification_preference_han_not_been_modified() {
        // Reset shared preferences
        clearSharedPrefs()
    }

    @When("^User open app settings screen$")
    fun user_open_app_settings_screen() {
        // Launch settings fragment
        scenario = launchFragmentInHiltContainer<SettingsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Alert Notification should be disabled$")
    fun alert_notification_should_be_disabled() {
        assertThat(sp.getBoolean(prefKey,true)).isFalse()
    }

    @When("^User enable weather alert notification$")
    fun user_enable_weather_alert_notification() {
        // Enable alert notification
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]!! as PreferenceFragmentCompat
            val prefUi = fragment.preferenceScreen.get<SwitchPreference>(prefKey)!!

            prefUi.performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should show alert notification upon weather alert$")
    fun app_should_show_alert_notification_upon_weather_alert() {
        // Verify notification pref is enabled
        assertThat(sp.getBoolean(prefKey,false)).isTrue()

        // Verify notification work has been scheduled
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfosForUniqueWork(
            WeatherAlertNotificationSchedulerImpl.WEATHER_ALERT_WORK_NAME).get()[0]

        assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)

        // Set notification work constrains and delay as met, for work to run
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setPeriodDelayMet(workInfo.id)
        testDriver.setAllConstraintsMet(workInfo.id)

        // Verify alert notification shown
        val notificationManager: NotificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications

        assertThat(notifications.size).isEqualTo(1)
        assertThat(notifications[0].notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo("Weather alert for your area:${alert.alertMessage}")
    }
}