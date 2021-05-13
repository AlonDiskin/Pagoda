package com.diskin.alon.pagoda.userjourney

import androidx.test.filters.LargeTest
import com.diskin.alon.pagoda.di.AppDataModule
import com.diskin.alon.pagoda.di.AppNetworkingModule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.inject.Inject

/**
 * Step definitions runner for 'User browse world location temperature' scenario.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class,WeatherNetworkingModule::class,AppDataModule::class)
@RunWith(Parameterized::class)
@LargeTest
class BrowseWorldLocationTemperatureStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/browse_world_location_temperature.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: TestDatabase

    @Test
    fun test() {
        hiltRule.inject()
        start(BrowseWorldLocationTemperatureSteps(db,NetworkUtil.server))
    }
}