package com.diskin.alon.pagoda.weatherinfo.featuretesting.showlatest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.di.InfrastructureModule
import com.diskin.alon.pagoda.weatherinfo.di.LocationModule
import com.diskin.alon.pagoda.weatherinfo.di.WeatherInfoNetworkingModule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.Scenario
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockWebServer
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.*
import javax.inject.Inject

/**
 * Step definitions com.diskin.alon.pagoda.runner for 'Latest location weather data shown' scenario.
 */
@HiltAndroidTest
@UninstallModules(WeatherInfoNetworkingModule::class,InfrastructureModule::class, LocationModule::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class,sdk = [28])
@MediumTest
class LatestLocationWeatherShownStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> {
            val res = ArrayList<Array<Any>>()
            val scenarioConfigs = GreenCoffeeConfig()
                .withFeatureFromAssets("feature/browse_current_location_weather.feature")
                .withTags("@latest-weather-shown")
                .scenarios()

            for (scenarioConfig in scenarioConfigs) {
                res.add(arrayOf(scenarioConfig))
            }

            return res
        }

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var locationProvider: UserLocationProvider

    @Inject
    lateinit var unitPrefProvider: WeatherUnitsEventProvider

    @Test
    fun test() {
        hiltRule.inject()
        start(LatestLocationWeatherShownSteps(mockWebServer,unitPrefProvider,locationProvider))
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        super.afterScenarioEnds(scenario, locale)
        mockWebServer.shutdown()
    }
}