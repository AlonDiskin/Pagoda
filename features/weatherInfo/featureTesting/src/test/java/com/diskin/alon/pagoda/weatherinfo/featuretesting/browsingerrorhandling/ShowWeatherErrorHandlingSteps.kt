package com.diskin.alon.pagoda.weatherinfo.featuretesting.browsingerrorhandling

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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.errors.DEVICE_LOCATION
import com.diskin.alon.pagoda.weatherinfo.errors.LOCATION_PERMISSION
import com.google.android.gms.common.api.ResolvableApiException
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import okhttp3.mockwebserver.*
import org.hamcrest.CoreMatchers.allOf
import org.robolectric.Shadows

/**
 * Step definitions for 'Weather data browsing error handled' scenario.
 */
class ShowWeatherErrorHandlingSteps(
    private val locationProvider: UserLocationProvider,
    private val server: MockWebServer,
    private val unitPrefProvider: WeatherUnitsEventProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
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

    @Given("^Existing error \"([^\"]*)\"$")
    fun existing_error(error: String) {
        // Set error type according to scenario argument
        when(error) {
            "app location permission" -> {
                val permissionError = Result.Error<UserLocation>(AppError(LOCATION_PERMISSION,true))
                every { locationProvider.getCurrentLocation() } returns Observable.just(permissionError)
            }

            "device location sensor" -> {
                val errorOrigin: ResolvableApiException = mockk()
                val pendingIntent: PendingIntent = mockk()
                val permissionError = Result.Error<UserLocation>(AppError(DEVICE_LOCATION,true,errorOrigin))

                every { errorOrigin.resolution } returns pendingIntent
                every { pendingIntent.intentSender } returns mockk()
                every { locationProvider.getCurrentLocation() } returns Observable.just(permissionError)
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
                val location = UserLocation(10.0, 10.0)
                every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()

                // Prepare unit system pref provider for scenario
                every { unitPrefProvider.get() } returns Observable.just(UnitSystemEvent.METRIC)
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
                val location = UserLocation(10.0, 10.0)
                every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()

                // Prepare unit system pref provider for scenario
                every { unitPrefProvider.get() } returns Observable.just(UnitSystemEvent.METRIC)
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${error}")
        }
    }

    @When("^User open current weather screen to browse weather$")
    fun user_open_current_weather_screen_to_browse_weather() {
        // Launch weather fragment
        val fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return WeatherFragment(testRegistry)
            }
        }
        scenario = launchFragmentInHiltContainer<WeatherFragment>(factory = fragmentFactory)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should handle error accordingly \"([^\"]*)\"$")
    fun app_should_handle_error_accordingly(handling: String) {
        when(handling) {
            "prompt user for location permission" -> {
                assertThat(testRegistry.isPermissionRequested).isTrue()
            }

            "prompt user for location setting" -> {
                assertThat(testRegistry.isLocationSettingRequested).isTrue()
            }

            "prompt user to retry later" -> {
                onView(withId(R.id.snackbar_text))
                    .check(
                        matches(
                            allOf(
                                withText(R.string.remote_server_error),
                                isDisplayed()
                            )
                        )
                    )
            }

            "prompt user to connect to network" -> {
                onView(withId(R.id.snackbar_text))
                    .check(
                        matches(
                            allOf(
                                withText(R.string.device_network_error),
                                isDisplayed()
                            )
                        )
                    )
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${handling}")
        }
    }
}