package com.diskin.alon.pagoda.userjourney

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat12
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat24
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
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
 * Step definitions for 'User changes weather units' scenario.
 */
class ChangeWeatherUnitsSteps(private val server: MockWebServer) : GreenCoffeeSteps() {

    private val dispatcher = TestDispatcher()

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)
    }

    @Given("^User has not changed default units preferences$")
    fun user_has_not_changed_default_units_preferences() {
        clearSharedPrefs()
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)
    }

    @And("^Open settings screen$")
    fun open_settings_screen() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_settings))
            .perform(click())
    }

    @When("^User change temperature and time format units$")
    fun user_change_temperature_and_time_format_units() {
        onView(withText(R.string.pref_temperature_title))
            .perform(click())

        onView(withText(R.string.pref_temperature_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())

        onView(withText(R.string.pref_time_format_title))
            .perform(click())

        onView(withText(R.string.pref_time_format_12_value))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
    }

    @And("^Open location weather screen$")
    fun open_location_weather_screen() {
        DeviceUtil.pressBack()
    }

    @Then("^Current temp and time should be updated according to units change$")
    fun current_temp_and_time_should_be_updated_according_to_units_change() {
        val weatherJson = FileUtil.readStringFromFile(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val expectedTemp = String.format("%.1f", ((currentTemp * (9.0 / 5.0)) + 32))
            .toDouble().toInt().toString().plus("°")

        Thread.sleep(1000)
        // Verify current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText(expectedTemp)))

        // Verify current location time shown in 12 hor format
        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12("E, dd MMM yyyy hh:mm aa")))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24(null)))

    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
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