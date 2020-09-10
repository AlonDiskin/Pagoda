package com.diskin.alon.pagoda.locationweather

import androidx.test.filters.LargeTest
import com.diskin.alon.pagoda.locationweather.UserLocationWeatherSteps
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Step definitions runner for 'User check weather for his location' scenario.
 */
@RunWith(Parameterized::class)
@LargeTest
class UserLocationWeatherStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/location_weather_journey.feature")
                .scenarios()
        }
    }

    @Test
    fun test() {
        start(UserLocationWeatherSteps())
    }
}