package com.diskin.alon.pagoda.settings.featuretesting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.diskin.alon.pagoda.settings.di.SettingsDataModule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.settings.featuretesting.di.TestInfrastructureModule
import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.*
import javax.inject.Inject

/**
 * Step runner for 'User enable alert notification' scenario.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class,SettingsDataModule::class,TestInfrastructureModule::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class,sdk = [28])
@MediumTest
class EnableWeatherAlertStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> {
            val res = ArrayList<Array<Any>>()
            val scenarioConfigs = GreenCoffeeConfig()
                .withFeatureFromAssets("feature/enabling_weather_alert.feature")
                .scenarios()

            for (scenarioConfig in scenarioConfigs) {
                res.add(arrayOf(scenarioConfig))
            }

            return res
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var weatherAlertProvider: WeatherAlertProvider

    @Test
    fun test() {
        hiltRule.inject()
        start(EnableWeatherAlertSteps(weatherAlertProvider))
    }
}