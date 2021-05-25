package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/**
 * Step definitions for 'User bookmark location' scenario.
 */
class BookmarkWorldLocationSteps(
    private val db: TestDatabase,
    server: MockWebServer
) : GreenCoffeeSteps() {

    private val location = createDbLocation()

    init {
        // Prepare test server
        server.setDispatcher(TestDispatcher())

        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                "VALUES(${location.lat},${location.lon},'${location.name}','${location.country}','${location.state}',0);"

        db.compileStatement(insertSql).executeInsert()
    }

    @Given("^User has no bookmarked locations$")
    fun user_has_no_bookmarked_locations() {
        // Verify test db does not contain any bookmarked locations
    }

    @And("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)
    }

    @And("^Open locations screen$")
    fun open_locations_screen() {
        onView(withId(R.id.drawerLayout))
            .perform(open())

        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.nav_locations))
        Thread.sleep(1000)
    }

    @And("^Select to add a new location$")
    fun select_to_add_a_new_location() {
        onView(withId(R.id.add_fab))
            .perform(click())
        Thread.sleep(1000)
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
        Thread.sleep(1000)
    }

    @And("^Select to bookmark it$")
    fun select_to_bookmark_it() {
        onView(withId(R.id.add_bookmark_button))
            .perform(click())
        Thread.sleep(1000)
    }

    @Then("^App should bookmark location and update locations list$")
    fun app_should_bookmark_location_and_update_locations_list() {
        // Verify location was bookmarked in db
        val bookmarked = db.compileStatement("SELECT COUNT(*) FROM locations WHERE bookmarked = 1")
            .simpleQueryForLong()

        assertThat(bookmarked).isEqualTo(1)

        // Close search view
        DeviceUtil.pressBack()

        // Nav back to bookmarked locations screen
        DeviceUtil.pressBack()

        // Verify bookmarked listed locations ui is updated
        onView(withId(com.diskin.alon.pagoda.locations.presentation.R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(1)))
    }

    private fun createDbLocation(): DbLocation {
        return DbLocation(
            36.213001,
            49.195999,
            "London",
            "GB",
            ""
        )
    }

    private data class DbLocation(
        val lat: Double,
        val lon: Double,
        val name: String,
        val country: String,
        val state: String
    )

    private class TestDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse().setResponseCode(404)
        }
    }
}