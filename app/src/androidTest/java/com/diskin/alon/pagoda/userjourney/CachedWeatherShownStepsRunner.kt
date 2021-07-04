package com.diskin.alon.pagoda.userjourney

import android.Manifest
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.AppDatabase
import com.diskin.alon.pagoda.di.TestSettingsNetworkingModule
import com.diskin.alon.pagoda.di.TestWeatherNetworkingModule
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
import javax.inject.Inject

/**
 * Step definitions runner for 'Cached weather data is shown' scenario.
 */
@HiltAndroidTest
@UninstallModules(TestWeatherNetworkingModule::class,TestSettingsNetworkingModule::class)
@RunWith(Parameterized::class)
@LargeTest
class CachedWeatherShownStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/show_cached_weather.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Inject
    lateinit var db: AppDatabase

    @Test
    fun test() {
        hiltRule.inject()
        start(CachedWeatherShownSteps(db))
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        super.afterScenarioEnds(scenario, locale)
        db.compileStatement("DELETE FROM current_weather").execute()
    }
}