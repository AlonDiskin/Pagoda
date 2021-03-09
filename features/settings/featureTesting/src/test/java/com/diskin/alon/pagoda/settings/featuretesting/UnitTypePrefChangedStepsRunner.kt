package com.diskin.alon.pagoda.settings.featuretesting

import androidx.test.filters.MediumTest
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventPublisher
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.*
import javax.inject.Inject

/**
 * Step definitions com.diskin.alon.pagoda.runner for 'User change unit type preference' scenario.
 */
@HiltAndroidTest
@RunWith(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class,sdk = [28])
@MediumTest
class UnitTypePrefChangedStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> {
            val res = ArrayList<Array<Any>>()
            val scenarioConfigs = GreenCoffeeConfig()
                .withFeatureFromAssets("weather_units_type_selection.feature")
                .withTags("@unit-selected")
                .scenarios()

            for (scenarioConfig in scenarioConfigs) {
                res.add(arrayOf(scenarioConfig))
            }

            return res
        }
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var eventPublisher: WeatherUnitsEventPublisher

    @Test
    fun test() {
        hiltRule.inject()
        start(UnitTypePrefChangedSteps(eventPublisher))
    }
}