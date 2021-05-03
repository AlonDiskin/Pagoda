package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
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

    @Then("^Location temperature should be shown in weather info screen$")
    fun location_temperature_should_be_shown_according_to_selected_unit() {
        // Extract expected temp value from mock server stub data
        val weatherJson = FileUtil.readStringFromFile(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val expectedTemp = currentTemp.toInt().toString().plus("°")

        Thread.sleep(3000)
        // Verify current temp shown as expected
        onView(withId(R.id.currentTemp))
            .check(matches(withText(expectedTemp)))

        // Verify server request count
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