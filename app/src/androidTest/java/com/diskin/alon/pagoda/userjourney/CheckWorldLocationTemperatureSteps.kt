package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsAdapter.LocationViewHolder
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Step definitions for 'User browse world location temperature' scenario.
 */
class CheckWorldLocationTemperatureSteps(server: MockWebServer) : GreenCoffeeSteps() {
    private val dispatcher = ScenarioDispatcher()

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @And("^User search for a world location$")
    fun user_search_for_a_world_location() {
        // Open search screen
        onView(withId(R.id.action_locations))
            .perform(click())

        // Perform search
        onView(withId(R.id.action_search))
            .perform(click())

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText("ashdod"))
    }

    @When("^User select to browse location result weather$")
    fun user_select_to_browse_result() {
        // Click on first search result as it is expected one for result
        onView(withId(R.id.locations))
            .perform(actionOnItemAtPosition<LocationViewHolder>(0, click()))
    }

    @Then("^Selected location temperature should be shown$")
    fun selected_location_temperature_should_be_shown() {
        val weatherJson = FileUtil.readStringFromFile(dispatcher.selectedWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")

        // Verify current temp show
        onView(withId(R.id.currentTemp))
            .check(matches(withText(currentTemp.roundToInt().toString().plus("°"))))
    }

    private class ScenarioDispatcher : NetworkUtil.TestDispatcher() {
        val selectedWeatherRes = "assets/json/weather_2.json"
        private val selectedGeoRes = "assets/json/geocoding_2.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"
        private val selectedLongitude = 34.650002
        private val selectedLatitude = 31.816669

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameter("lat") == selectedLatitude.toString()  &&
                        request.requestUrl.queryParameter("lon") == selectedLongitude.toString()  &&
                        request.requestUrl.queryParameterNames().contains("lon") &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(selectedWeatherRes))
                            .setResponseCode(200)

                    } else {
                        super.dispatch(request)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameter("lat") == selectedLatitude.toString()  &&
                        request.requestUrl.queryParameter("lon") == selectedLongitude.toString()  &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(selectedGeoRes))
                            .setResponseCode(200)

                    } else {
                        super.dispatch(request)
                    }
                }

                else -> super.dispatch(request)
            }
        }
    }
}