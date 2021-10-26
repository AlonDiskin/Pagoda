package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf

/**
 * Step definitions for 'User favorite location' scenario.
 */
@ExperimentalCoroutinesApi
class FavoriteWorldLocationSteps : GreenCoffeeSteps() {

    @Given("^User has no favorite locations$")
    fun user_has_no_favorite_locations() {
        // Verify test db does not contain any bookmarked locations
        // TODO
    }

    @And("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @And("^Open search locations screen$")
    fun open_search_screen() {
        onView(withId(R.id.action_locations))
            .perform(click())
    }

    @When("^User find wanted location$")
    fun user_find_wanted_location() {
        onView(withId(R.id.action_search))
            .perform(click())

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText("london"))
    }

    @And("^Select to favorite it$")
    fun select_to_bookmark_it() {
        onView(allOf(
            withId(R.id.favorite_button),
            hasSibling(withText("London")),
            hasSibling(withText("United Kingdom"))
        ))
            .perform(click())
    }

    @Then("^App should favorite location and update favorites list$")
    fun app_should_favorite_location_and_update_favorites_list() {
        // Close search view
        DeviceUtil.pressBack()
        DeviceUtil.pressBack()
        Thread.sleep(1000)

        // Verify favorite listed locations ui is updated
        onView(withId(R.id.locations))
            .check(matches(isRecyclerViewItemsCount(1)))
        onView(withText("London"))
            .check(matches(isDisplayed()))
        onView(withText("United Kingdom"))
            .check(matches(isDisplayed()))
    }

    @When("^User select to unfavorite added location$")
    fun user_select_to_unfavorite_added_location() {
        // Unfavorite bookmark
        onView(withId(R.id.favorite_button))
            .perform(click())

        onView(withText("OK"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
    }

    @Then("^App should remove location from user favorites listing$")
    fun app_should_remove_location_from_user_favorites_listing() {
        // Verify favorite listed locations ui is updated
        onView(withId(R.id.locations))
            .check(matches(isRecyclerViewItemsCount(0)))
    }
}