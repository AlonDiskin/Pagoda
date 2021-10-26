package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.util.DeviceUtil
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

/**
 * Step definitions for 'User enables dark mode' scenario.
 */
class SetDarkThemeSteps : GreenCoffeeSteps() {

    @Given("^User has not changed default preferences$")
    fun user_has_not_changed_default_preferences() {
        DeviceUtil.clearSharedPrefs()
    }

    @And("^User launch app from device home$")
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

    @When("^User enables dark mode$")
    fun user_enables_dark_mode() {
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.pref_dark_mode_title)),
                    click()
                )
            )
    }

    @Then("^App theme should change to dark$")
    fun app_theme_should_change_to_dark() {
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @When("^User relaunches app from home$")
    fun user_relaunches_app_from_home() {
        DeviceUtil.pressBack()
        DeviceUtil.pressBack()
        DeviceUtil.launchAppFromHome()
    }

    @Then("^App theme should remain dark$")
    fun app_theme_should_remain_dark() {
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }
}