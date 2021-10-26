package com.diskin.alon.pagoda.userjourney

import android.Manifest
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.Scenario
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Step definitions runner for 'User enables dark mode' scenario.
 */
@HiltAndroidTest
@UninstallModules(WeatherNetworkingModule::class)
@RunWith(Parameterized::class)
@LargeTest
class SetDarkThemeStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/set_dark_theme.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Test
    fun test() {
        // Set test work manager,since work manager is created and injected, when settings screen
        // is opened and alert enabling listener service is created
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(getApplicationContext(), config)
        start(SetDarkThemeSteps())
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        super.afterScenarioEnds(scenario, locale)
        DeviceUtil.clearSharedPrefs()
    }
}