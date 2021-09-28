package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf

/**
 * Step definitions for 'User bookmark location' scenario.
 */
@ExperimentalCoroutinesApi
class BookmarkWorldLocationSteps : GreenCoffeeSteps() {

    @Given("^User has no bookmarked locations$")
    fun user_has_no_bookmarked_locations() {
        // Verify test db does not contain any bookmarked locations
        // TODO
    }

    @And("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()
    }

    @And("^Open locations screen$")
    fun open_locations_screen() {
        onView(withId(R.id.action_bookmarks))
            .perform(click())
    }

    @And("^Select to add a new location$")
    fun select_to_add_a_new_location() {
        onView(withId(R.id.action_add))
            .perform(click())
    }

    @Then("^App should open world locations search screen$")
    fun app_should_open_world_locations_search_screen() {
        onView(withId(R.id.search_location_results))
            .check(matches(isDisplayed()))
    }

    @When("^User find wanted location$")
    fun user_find_wanted_location() {
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText("london"))
    }

    @And("^Select to bookmark it$")
    fun select_to_bookmark_it() {
        onView(allOf(
            withId(R.id.add_bookmark_button),
            hasSibling(withText("London")),
            hasSibling(withText("GB"))
        ))
            .perform(click())
    }

    @Then("^App should bookmark location and update locations list$")
    fun app_should_bookmark_location_and_update_locations_list() {
        // Close search view
        DeviceUtil.pressBack()

        // Nav back to bookmarked locations screen
        DeviceUtil.pressBack()

        // Verify bookmarked listed locations ui is updated
        onView(withId(R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(1)))
        onView(withText("London"))
            .check(matches(isDisplayed()))
        onView(withText("GB"))
            .check(matches(isDisplayed()))
    }

    @When("^User select to remove bookmark$")
    fun user_select_to_remove_bookmark() {
        // Remove bookmark
        onView(withId(R.id.locationOptions))
            .perform(click())

        onView(withText("Delete"))
            .perform(click())

        onView(withText("OK"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
    }

    @Then("^App should remove bookmark from user bookmarks listing$")
    fun app_should_remove_bookmark_from_user_bookmarks_listing() {
        // Verify bookmarked listed locations ui is updated
        onView(withId(R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(0)))
    }
}