package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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

    @And("^Open search locations screen$")
    fun open_search_screen() {
        onView(withId(R.id.action_search))
            .perform(click())
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
            hasSibling(withText("United Kingdom"))
        ))
            .perform(click())
    }

    @Then("^App should bookmark location and update bookmarks list$")
    fun app_should_bookmark_location_and_update_bookmarks_list() {
        // Close search view
        DeviceUtil.pressBack()

        // Nav back to weather screen
        DeviceUtil.pressBack()

        // Open bookmarks screen
        onView(withId(R.id.action_bookmarks))
            .perform(click())

        // Verify bookmarked listed locations ui is updated
        onView(withId(R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(1)))
        onView(withText("London"))
            .check(matches(isDisplayed()))
        onView(withText("United Kingdom"))
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