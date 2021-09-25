package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_current_location_weather

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.createCachedWeather
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.verifyDbWeatherShown
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.verifyServerWeatherShown
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
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
import io.reactivex.subjects.SingleSubject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.LocalDateTime
import org.robolectric.Shadows
import java.util.*

/**
 * Step definitions for 'Latest weather shown for user location' scenario.
 */
class LatestWeatherShownSteps(
    private val server: MockWebServer,
    private val db: TestDatabase,
    private val locationProvider: UserLocationProvider,
    tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Stub data
    private val locationSubject = SingleSubject.create<Result<UserLocation>>()
    private lateinit var userLocation: UserLocation
    private val cachedWeather = createCachedWeather()
    private lateinit var dispatcher: TestDispatcher

    init {
        // Stub location provider
        every { locationProvider.getLocation() } returns locationSubject

        // Stub mock app prefs providers
        every { tempUnitPrefProvider.get() } returns Observable.just(TemperatureUnitPref(
            UnitPrefSystem.METRIC))
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(WindSpeedUnitPref(
            UnitPrefSystem.METRIC))
        every { timeFormatPrefProvider.get() } returns Observable.just(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))

        // Stub static image loader
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit
    }

    @Given("^App has \"([^\"]*)\" weather for last user location$")
    fun app_has_cached_weather_for_last_user_location(isCachedWeather: String) {
        // Set test db
        when(isCachedWeather) {
            "cached" -> db.currentWeatherDao().insert(cachedWeather).blockingAwait()
            "not cached" -> {}
            else -> throw IllegalArgumentException("Unknown step argument:${isCachedWeather}")
        }
    }

    @And("^User location since last app usage has been \"([^\"]*)\"$")
    fun user_location_since_last_app_usage_has_been_something(locationChange: String) {
        // Set stub user location and test dispatcher based on scenario
        userLocation = when(locationChange) {
            "changed" -> UserLocation(cachedWeather.lat + 5.5,cachedWeather.lon + 5.5)
            "not changed" -> UserLocation(cachedWeather.lat,cachedWeather.lon)
            else -> throw IllegalArgumentException("Unknown step argument:${locationChange}")
        }

        dispatcher = TestDispatcher(userLocation.lat,userLocation.lon)
        server.setDispatcher(dispatcher)
    }

    @And("^Weather update for user location is \"([^\"]*)\"$")
    fun weather_update_for_user_location_is_available(weatherUpdate: String) {
        val calendar = mockk<Calendar>()
        val current = when(weatherUpdate) {
            "available" -> LocalDateTime(cachedWeather.updated).plusHours(5).toDate().time
            "not available" -> LocalDateTime(cachedWeather.updated).plusMinutes(10).toDate().time
            else -> throw IllegalArgumentException("Unknown step argument:${weatherUpdate}")
        }

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns current
    }

    @When("^User open current weather screen$")
    fun user_open_current_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should \"([^\"]*)\" weather$")
    fun app_should_show_cached_weather(showCached: String) {
        // Verify cached weather shown
        when(showCached) {
            "show" -> verifyDbWeatherShown(db, scenario)
            "not show" -> verifyWeatherNotShown()
            else -> throw IllegalArgumentException("Unknown step argument:${showCached}")
        }
    }

    @And("^App should \"([^\"]*)\" cached and shown weather$")
    fun app_should_update_cached_and_shown_weather(updateWeather: String) {
        locationSubject.onSuccess(Result.Success(userLocation))

        when(updateWeather) {
            "update" -> verifyServerWeatherShown(
                dispatcher.locationWeatherRes,
                dispatcher.locationGeoRes,
                scenario)
            "not update" -> verifyDbWeatherShown(db, scenario)
            else -> throw IllegalArgumentException("Unknown step argument:${updateWeather}")
        }
    }

    private fun verifyWeatherNotShown() {
        onView(withId(R.id.weatherMain))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.nestedScrollView))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    private class TestDispatcher(private val locationLat: Double,private val locationLon: Double): Dispatcher() {
        val locationWeatherRes = "json/current_weather_location1.json"
        val locationGeoRes = "json/geocoding_location1.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                        request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(getJsonFromResource(locationWeatherRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                        request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(getJsonFromResource(locationGeoRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}