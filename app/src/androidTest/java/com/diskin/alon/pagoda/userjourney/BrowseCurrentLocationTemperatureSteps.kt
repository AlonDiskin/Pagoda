package com.diskin.alon.pagoda.userjourney

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject

/**
 * Step definitions for 'User browse current location Temperature' scenario.
 */
class BrowseCurrentLocationTemperatureSteps(private val server: MockWebServer) : GreenCoffeeSteps() {
    private val dispatcher = TestDispatcher()

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @When("^User select different weather units system$")
    fun user_select_different_weather_units_system() {
        // Select imperial units from app settings
        // Currently (4.2021)  openActionBarOverflowOrOptionsMenu method fail sometimes
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_settings))
            .perform(click())
        onView(withText(R.string.pref_units_title))
            .perform(click())
        onView(withText(R.string.pref_units_imperial_entry))
            .inRoot(isDialog())
            .perform(click())
    }

    @And("^Check temperature for current location")
    fun check_temperature_for_current_location() {
        // Return to app home(weather info screen)
        DeviceUtil.pressBack()
    }

    @Then("^Location temperature should be shown according to selected unit$")
    fun location_temperature_should_be_shown_according_to_selected_unit() {
        val weatherJson = FileUtil.readStringFromFile(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")

        // Verify current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText(
                String.format("%.1f", ((currentTemp * (9.0/5.0)) + 32))
                    .toDouble().toInt().toString().plus("°")
            )))

        assertThat(server.requestCount).isEqualTo(2)
    }

    private class TestDispatcher: Dispatcher() {
        val locationWeatherRes = "assets/json/location_weather.json"
        val locationGeoRes = "assets/json/location_geocoding.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameterNames().contains("lat") &&
                        request.requestUrl.queryParameterNames().contains("lon") &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(locationWeatherRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameterNames().contains("lat") &&
                        request.requestUrl.queryParameterNames().contains("lon") &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(locationGeoRes))
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