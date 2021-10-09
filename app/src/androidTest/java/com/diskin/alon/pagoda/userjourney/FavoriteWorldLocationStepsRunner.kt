package com.diskin.alon.pagoda.userjourney

import android.Manifest
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Step definitions runner for 'User favorite location' scenario.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class, WeatherNetworkingModule::class)
@RunWith(Parameterized::class)
@LargeTest
@ExperimentalCoroutinesApi
class FavoriteWorldLocationStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/favorite_world_location.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Test
    fun test() {
        start(FavoriteWorldLocationSteps())
    }
}