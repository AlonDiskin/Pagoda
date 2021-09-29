package com.diskin.alon.pagoda.userjourney

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.events.SharedPrefsEventHandler
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat12
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat24
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Step definitions for 'User changes weather units' scenario.
 */
class ChangeWeatherUnitsSteps : GreenCoffeeSteps() {

    @Given("^User has not changed default units preferences$")
    fun user_has_not_changed_default_units_preferences() {
        clearSharedPrefs()
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @And("^Open settings screen$")
    fun open_settings_screen() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_settings))
            .perform(click())
    }

    @When("^User change temperature and time format units$")
    fun user_change_temperature_and_time_format_units() {
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.pref_temperature_title)),
                click())
            )

        onView(withText(R.string.pref_temperature_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.pref_time_format_title)),
                    click())
            )

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
        val weatherJson = FileUtil.readStringFromFile(NetworkUtil.dispatcher.weatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val expectedTemp = String.format("%.1f", ((currentTemp * (9.0 / 5.0)) + 32))
            .toDouble().roundToInt().toString().plus("°")

        Thread.sleep(5000)

        // Verify current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText(expectedTemp)))

        // Verify current temp unit
        onView(withId(R.id.currentTempUnit))
            .check(matches(withText("F")))

        // Verify current location time shown in 12 hor format
        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12("E, dd MMM yyyy hh:mm aa")))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24(null)))
    }

    fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences(SharedPrefsEventHandler.EVENTS_FILE,Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}