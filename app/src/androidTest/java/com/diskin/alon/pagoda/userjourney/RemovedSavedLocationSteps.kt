package com.diskin.alon.pagoda.userjourney

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/**
 * Step definitions for 'User remove saved location' scenario.
 */
class RemovedSavedLocationSteps(
    server: MockWebServer,
    private val db: TestDatabase
) : GreenCoffeeSteps() {

    private val dispatcher = TestDispatcher()
    private val locations = createDbLocations()

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)
    }

    @Given("^User has previously saved locations$")
    fun user_has_previously_saved_locations() {
        locations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}',1);"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @And("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @And("^Open locations screen$")
    fun open_locations_screen() {
        onView(withId(R.id.drawerLayout))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.nav_locations))
        Thread.sleep(1000)
    }

    @And("^Removes first listed location$")
    fun removes_first_listed_location() {
        onView(withId(R.id.locationOptions))
            .perform(click())

        onView(withText(R.string.title_action_location_delete))
            .perform(click())

        onView(withText("OK"))
        .inRoot(isDialog())
            .perform(click())
    }

    @Then("^App should show updated locations accordingly$")
    fun app_should_show_updated_locations_accordingly() {
        Thread.sleep(2000)
        onView(withId(R.id.bookmarked_locations))
            .check(ViewAssertions.matches(isRecyclerViewItemsCount(locations.size - 1)))
    }

    private fun createDbLocations(): List<DbLocation> {
        return listOf(
            DbLocation(
                36.213001,
                49.195999,
                "Losevo",
                "Russia",
                ""
            )
        )
    }

    private data class DbLocation(
        val lat: Double,
        val lon: Double,
        val name: String,
        val country: String,
        val state: String
    )

    private data class DbBookmarkedLocation(val lat: Double, val lon: Double)

    private class TestDispatcher : Dispatcher() {

        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse().setResponseCode(404)
        }
    }
}