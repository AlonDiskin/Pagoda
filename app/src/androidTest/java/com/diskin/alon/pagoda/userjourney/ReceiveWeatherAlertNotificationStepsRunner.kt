package com.diskin.alon.pagoda.userjourney

import android.Manifest
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.inject.Inject

/**
 * Step definitions runner for 'User receive weather alert notification' scenario.
 */
@HiltAndroidTest
@UninstallModules(WeatherNetworkingModule::class)
@RunWith(Parameterized::class)
@LargeTest
class ReceiveWeatherAlertNotificationStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/receive_weather_alert_notification.feature")
                .scenarios()
        }

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Inject
    lateinit var alertProvider: WeatherAlertProvider

    @Test
    fun test() {
        hiltRule.inject()
        start(ReceiveWeatherAlertNotificationSteps(NetworkUtil.server,alertProvider))
    }
}