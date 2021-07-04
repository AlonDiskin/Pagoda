package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject

/**
 * Step definitions for 'User browse current location Temperature' scenario.
 */
class BrowseCurrentLocationTemperatureSteps(private val server: MockWebServer) : GreenCoffeeSteps() {

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @Then("^Location temperature should be shown in weather info screen$")
    fun location_temperature_should_be_shown_according_to_selected_unit() {
        // Extract expected temp value from mock server stub data
        val weatherJson = FileUtil.readStringFromFile(NetworkUtil.dispatcher.weatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val expectedTemp = currentTemp.toInt().toString().plus("°")

        // Verify current temp shown as expected
        onView(withId(R.id.currentTemp))
            .check(matches(withText(expectedTemp)))
    }
}