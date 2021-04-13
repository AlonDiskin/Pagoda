package com.diskin.alon.pagoda.userjourney

import androidx.test.filters.LargeTest
import com.diskin.alon.pagoda.di.AppDataModule
import com.diskin.alon.pagoda.di.AppNetworkingModule
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weatherinfo.di.WeatherInfoNetworkingModule
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

/**
 * Step definitions runner for 'User check location temperature in different unit system' scenario.
 */
@HiltAndroidTest
@UninstallModules(AppNetworkingModule::class,WeatherInfoNetworkingModule::class, AppDataModule::class)
@RunWith(Parameterized::class)
@LargeTest
class BrowseLocationTempStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/check_temp_in_different_units.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun test() {
        start(BrowseLocationTempSteps(NetworkUtil.server))
    }
}