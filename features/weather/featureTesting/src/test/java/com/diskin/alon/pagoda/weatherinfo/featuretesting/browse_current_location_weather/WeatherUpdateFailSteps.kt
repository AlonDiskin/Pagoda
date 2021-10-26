package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_current_location_weather

import android.Manifest
import android.app.PendingIntent
import android.os.Looper
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.createCachedWeather
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.verifyDbWeatherShown
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.mockwebserver.*
import org.joda.time.LocalDateTime
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast
import java.util.*

/**
 * Step definitions for 'Weather update fail' scenario.
 */
class WeatherUpdateFailSteps(
    private val server: MockWebServer,
    private val db: TestDatabase,
    private val locationProvider: UserLocationProvider,
    private val tempUnitProvider: AppDataProvider<Observable<TempUnit>>,
    private val windSpeedUnitProvider: AppDataProvider<Observable<WindSpeedUnit>>,
    private val timeFormatProvider: AppDataProvider<Observable<TimeFormat>>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Stub data
    private lateinit var location: UserLocation
    private val cachedWeather = createCachedWeather()
    private val testRegistry = object : ActivityResultRegistry() {
        var isPermissionRequested = false
        var isLocationSettingRequested = false

        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            when(contract) {
                is ActivityResultContracts.RequestPermission -> {
                    if (input!! == Manifest.permission.ACCESS_FINE_LOCATION) {
                        isPermissionRequested = true
                    }
                }

                is ActivityResultContracts.StartIntentSenderForResult -> {
                    isLocationSettingRequested = true
                }
            }
        }
    }

    init {
        // Stub mock app prefs providers
        every { tempUnitProvider.get() } returns Observable.just(
            TempUnit(
            UnitSystem.METRIC)
        )
        every { windSpeedUnitProvider.get() } returns Observable.just(
            WindSpeedUnit(
            UnitSystem.METRIC)
        )
        every { timeFormatProvider.get() } returns Observable.just(
            TimeFormat(
                TimeFormat.HourFormat.HOUR_24)
        )

        // Stub static image loader
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit
    }

    @Given("^App has cached location weather data$")
    fun app_has_cached_location_weather_data() {
        // Set test db
        db.currentWeatherDao().insert(cachedWeather).blockingAwait()
    }

    @And("^Weather update for user location is available$")
    fun weather_update_for_user_location_is_available() {
        location = UserLocation(cachedWeather.lat,cachedWeather.lon)
        val calendar = mockk<Calendar>()
        val current = LocalDateTime(cachedWeather.updated).plusHours(1).toDate().time

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns current
    }

    @And("^Existing error \"([^\"]*)\" that fail update$")
    fun existing_error_type_that_fail_update(error: String) {
        when(error) {
            "app location permission" -> {
                val permissionError = Result.Error<UserLocation>(AppError(ErrorType.LOCATION_PERMISSION))
                every { locationProvider.getLocation() } returns Single.just(permissionError)
            }

            "device location services" -> {
                val errorOrigin: ResolvableApiException = mockk()
                val pendingIntent: PendingIntent = mockk()
                val permissionError = Result.Error<UserLocation>(AppError(ErrorType.DEVICE_LOCATION,errorOrigin))

                every { errorOrigin.resolution } returns pendingIntent
                every { pendingIntent.intentSender } returns mockk()
                every { locationProvider.getLocation() } returns Single.just(permissionError)
            }

            "device network" -> {
                // Prepare test server for scenario
                val dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                    }
                }

                server.setDispatcher(dispatcher)

                // Prepare location provider for scenario
                every { locationProvider.getLocation() } returns Single.just(Result.Success(location))
            }

            "remote server" -> {
                // Prepare test server for scenario
                val dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return MockResponse().setResponseCode(500)
                    }
                }

                server.setDispatcher(dispatcher)

                // Prepare location provider for scenario
                every { locationProvider.getLocation() } returns Single.just(Result.Success(location))
            }

            else -> throw IllegalArgumentException("Unknown step argument:${error}")
        }
    }

    @When("^User open current weather screen$")
    fun user_open_current_weather_screen() {
        // Launch weather fragment
        val fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return WeatherFragment(testRegistry)
            }
        }
        scenario = launchFragmentInHiltContainer<WeatherFragment>(factory = fragmentFactory)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should show cached weather$")
    fun app_should_show_cached_weather() {
        // Verify cached weather data shown in ui
        verifyDbWeatherShown(db, scenario)
    }

    @Then("^App should provide \"([^\"]*)\"$")
    fun app_should_provide_error_recovery(errorRecovery: String) {
        when(errorRecovery) {
            "ask user for location permission" -> {
                assertThat(testRegistry.isPermissionRequested).isTrue()
            }

            "ask user to enable location services" -> {
                assertThat(testRegistry.isLocationSettingRequested).isTrue()
            }

            "notify remote server error" -> {
                assertThat(ShadowToast.getTextOfLatestToast())
                    .isEqualTo("Server error,please try later")
            }

            "notify network connection error" -> {
                assertThat(ShadowToast.getTextOfLatestToast())
                    .isEqualTo("No network connection")
            }

            else -> throw IllegalArgumentException("Unknown step argument:${errorRecovery}")
        }
    }
}