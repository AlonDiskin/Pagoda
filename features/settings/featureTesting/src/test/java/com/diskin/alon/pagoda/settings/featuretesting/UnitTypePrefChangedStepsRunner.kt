package com.diskin.alon.pagoda.settings.featuretesting

import androidx.test.filters.MediumTest
import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.settings.di.SettingsDataModule
import com.diskin.alon.pagoda.settings.di.SettingsInfrastructureModule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
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
 * Step runner for 'User change unit type preference' scenario.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class, SettingsDataModule::class,SettingsInfrastructureModule::class)
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
                .withFeatureFromAssets("feature/weather_units_type_selection.feature")
                .withTags("@unit-changed")
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
    lateinit var tempUnitPublisher: AppEventPublisher<TemperatureUnitPref>

    @Inject
    lateinit var windSpeedUnitPublisher: AppEventPublisher<WindSpeedUnitPref>

    @Inject
    lateinit var timeFormatPublisher: AppEventPublisher<TimeFormatPref>

    @Test
    fun test() {
        hiltRule.inject()
        start(UnitTypePrefChangedSteps(tempUnitPublisher, windSpeedUnitPublisher, timeFormatPublisher))
    }
}