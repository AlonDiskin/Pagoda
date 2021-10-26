package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_world_location_weather

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ViewDataBinding
import androidx.test.filters.MediumTest
import com.diskin.alon.pagoda.weatherinfo.di.WeatherLocationModule
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.diskin.alon.pagoda.common.featuretesting.setFinalStatic
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.Scenario
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.reactivex.Observable
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
 * Step definitions runner for 'Latest weather shown for world location' scenario.
 */
@HiltAndroidTest
@UninstallModules(WeatherNetworkingModule::class, WeatherLocationModule::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class,sdk = [28])
@MediumTest
class WorldLocationWeatherShownStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> {
            val res = ArrayList<Array<Any>>()
            val scenarioConfigs = GreenCoffeeConfig()
                .withFeatureFromAssets("feature/browse_world_location_weather.feature")
                .withTags("@world-location-weather-shown")
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
    lateinit var tempUnitPrefProvider: AppDataProvider<Observable<TempUnit>>

    @Inject
    lateinit var windSpeedUnitPrefProvider: AppDataProvider<Observable<WindSpeedUnit>>

    @Inject
    lateinit var timeFormatPrefProvider: AppDataProvider<Observable<TimeFormat>>

    @Inject
    lateinit var locationProvider: UserLocationProvider

    @Test
    fun test() {
        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Inject test dependencies
        hiltRule.inject()

        // Start test
        start(
            WorldLocationWeatherShownSteps(
            mockWebServer,
            tempUnitPrefProvider,
            windSpeedUnitPrefProvider,
            timeFormatPrefProvider,
                locationProvider
            )
        )
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        super.afterScenarioEnds(scenario, locale)
        mockWebServer.shutdown()
    }
}