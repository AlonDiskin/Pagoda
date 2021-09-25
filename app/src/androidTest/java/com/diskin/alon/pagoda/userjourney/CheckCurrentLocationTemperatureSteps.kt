package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.AppDatabase
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject

/**
 * Step definitions for 'User check current location temperature' scenario.
 */
class CheckCurrentLocationTemperatureSteps : GreenCoffeeSteps() {

    @Given("^User launch app from device home for first time$")
    fun user_launch_app_from_device_home_for_first_time() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @Then("^App should show temperature for user location in metric units$")
    fun app_should_show_temperature_for_user_location_in_metric_units() {
        verifyServerTempShownAsMetric()
    }

    @When("^User leave app$")
    fun user_leave_app() {
        DeviceUtil.pressBack()
    }

    @And("^Device network is disconnected$")
    fun device_network_is_disconnected() {
        DeviceUtil.disconnectNetwork()
    }

    @When("^User relaunch app from home$")
    fun user_relaunch_app_from_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @Then("^Temperature for last location should be shown in metric units$")
    fun temperature_for_last_location_should_be_shown_in_metric_units() {
        verifyServerTempShownAsMetric()
    }

    private fun verifyServerTempShownAsMetric() {
        // Extract expected temp value from mock server stub data
        val weatherJson = FileUtil.readStringFromFile(NetworkUtil.dispatcher.weatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val expectedTemp = currentTemp.toInt().toString().plus("°")

        // Verify current temp shown as expected
        onView(withId(R.id.currentTemp))
            .check(matches(withText(expectedTemp)))

        // Verify current temp unit shown as expected
        onView(withId(R.id.currentTempUnit))
            .check(matches(withText("C")))
    }
}