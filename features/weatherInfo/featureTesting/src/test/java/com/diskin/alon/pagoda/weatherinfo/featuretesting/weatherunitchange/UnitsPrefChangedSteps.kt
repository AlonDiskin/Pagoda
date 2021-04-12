package com.diskin.alon.pagoda.weatherinfo.featuretesting.weatherunitchange

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.featuretesting.getJsonFromResource
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.robolectric.Shadows

/**
 * Step definitions for 'Weather units preference changed' scenario.
 */
class UnitsPrefChangedSteps(
    server: MockWebServer,
    private val unitPrefProvider: WeatherUnitsEventProvider,
    private val locationProvider: UserLocationProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()
    private lateinit var unitPrefSubject: BehaviorSubject<UnitSystemEvent>

    init {
        // Prepare mock server for test scenario
        server.setDispatcher(dispatcher)

        // Prepare location provider for test scenario
        val location = UserLocation(dispatcher.locationLat, dispatcher.locationLon)
        every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()
    }

    @Given("^Units preference is set as \"([^\"]*)\"$")
    fun units_preference_is_set_as_something(current: String) {
        unitPrefSubject = when(current) {
            "metric" -> BehaviorSubject.createDefault(UnitSystemEvent.METRIC)
            "imperial" -> BehaviorSubject.createDefault(UnitSystemEvent.IMPERIAL)
            else -> throw IllegalArgumentException("Unknown test step arg:${current}")
        }

        every { unitPrefProvider.get() } returns unitPrefSubject
    }

    @And("^User open current weather screen$")
    fun user_open_current_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Weather data units is shown according to \"([^\"]*)\" preference$")
    fun weather_data_units_is_shown_according_to_something_preference(current: String) {
        when(current) {
            "metric" -> checkDataShownAsMetric()
            "imperial" -> checkDataShownAsImperial()
            else -> throw IllegalArgumentException("Unknown test step arg:${current}")
        }
    }

    @When("^User change unit system preference to \"([^\"]*)\"$")
    fun user_change_unit_system_preference_to_something(selected: String) {
        when(selected) {
            "metric" -> unitPrefSubject.onNext(UnitSystemEvent.METRIC)
            "imperial" -> unitPrefSubject.onNext(UnitSystemEvent.IMPERIAL)
            else -> throw IllegalArgumentException("Unknown test step arg:${selected}")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Shown weather data should be updated to \"([^\"]*)\" units$")
    fun shown_weather_data_should_be_updated_to_something_units(selected: String) {
        when(selected) {
            "metric" -> checkDataShownAsMetric()
            "imperial" -> checkDataShownAsImperial()
            else -> throw IllegalArgumentException("Unknown test step arg:${selected}")
        }
    }

    private fun checkDataShownAsMetric() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current").getDouble("temp")
        val feelTemp = JSONObject(weatherJson).getJSONObject("current").getDouble("feels_like")
        val minTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("min")
        val maxTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("max")
        val windSpeed = JSONObject(weatherJson).getJSONObject("current").getDouble("wind_speed")

        // check current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${currentTemp.toInt()}°")))

        // check feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.toInt()}°")))

        // check min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°")))

        // check wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.toInt()}km/h")))
    }

    private fun checkDataShownAsImperial() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val currentTemp = celsiusToFahrenheit(JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp"))
        val feelTemp = celsiusToFahrenheit(JSONObject(weatherJson).getJSONObject("current")
            .getDouble("feels_like"))
        val minTemp = celsiusToFahrenheit(JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("min"))
        val maxTemp = celsiusToFahrenheit(JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("max"))
        val windSpeed = kphToMph(JSONObject(weatherJson).getJSONObject("current")
            .getDouble("wind_speed"))
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")
        val dailyForecast = JSONObject(weatherJson).getJSONArray("daily")

        // Verify current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${currentTemp.toInt()}°")))

        // Verify feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.toInt()}°")))

        // Verify min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°")))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.toInt()}mph")))

        // Verify hourly forecast temps
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(12)))

        for (i in 0 until 12) {
            val hourTemp = celsiusToFahrenheit(hourlyForecast.getJSONObject(i)
                .getDouble("temp"))

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(RecyclerViewMatcher.withRecyclerView(R.id.hourForecast).atPositionOnView(i,R.id.temp))
                .check(matches(withText("${hourTemp.toInt()}°")))
        }

        // Verify daily forecast temps
        onView(withId(R.id.dailyForecast))
            .check(matches(isRecyclerViewItemsCount(8)))

        for (i in 0 until 8) {
            val dayMinTemp = celsiusToFahrenheit(dailyForecast.getJSONObject(i).getJSONObject("temp")
                .getDouble("min"))
            val dayMaxTemp = celsiusToFahrenheit(dailyForecast.getJSONObject(i).getJSONObject("temp")
                .getDouble("max"))

            onView(RecyclerViewMatcher.withRecyclerView(R.id.dailyForecast).atPositionOnView(i,R.id.minTemp))
                .check(matches(withText("${dayMinTemp.toInt()}°")))

            onView(RecyclerViewMatcher.withRecyclerView(R.id.dailyForecast).atPositionOnView(i,R.id.maxTemp))
                .check(matches(withText("${dayMaxTemp.toInt()}°")))
        }
    }

    private class TestDispatcher: Dispatcher() {
        val locationWeatherRes = "json/current_weather_location1.json"
        val locationLat = 55.7558
        val locationLon = 37.6173
        private val locationGeoRes = "json/geocoding_location1.json"
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

    private fun celsiusToFahrenheit(celsius: Double): Double {
        return String.format("%.1f", ((celsius * (9.0/5.0)) + 32)).toDouble()
    }

    private fun kphToMph(kph: Double): Double {
        return String.format("%.1f", (kph / 1.6)).toDouble()
    }
}