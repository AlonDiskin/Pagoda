package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
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
 * Step definitions for 'User search world location weather' scenario.
 */
class SearchLocationSteps(db: TestDatabase,server: MockWebServer) : GreenCoffeeSteps() {

    private val query = "los angeles"
    private val locations = createDbLocations()

    init {
        // Prepare test server
        server.setDispatcher(TestDispatcher())

        // Prepare test database
        locations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}');"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @And("^User search for location other then his current one$")
    fun user_search_for_location_other_then_his_current_one() {
        // Open search screen
        onView(withId(R.id.action_search_location))
            .perform(click())

        // Perform search
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText(query))
    }

    @Then("^All matching locations by name should be shown$")
    fun all_matching_locations_by_name_should_be_shown() {
        val expectedUiResultsSize = locations.filter { it.name.startsWith(query,ignoreCase = true) }.size

        // Verify expected search results are shown
        onView(withId(R.id.searchResults))
            .check(matches(isRecyclerViewItemsCount(expectedUiResultsSize)))
    }

    private data class DbLocation(val lat: Double,
                                  val lon: Double,
                                  val name: String,
                                  val country: String,
                                  val state: String)

    private fun createDbLocations(): List<DbLocation> {
        return listOf(
            DbLocation(
                36.213001,
                49.195999,
                "Losevo",
                "Russia",
                ""
            ),
            DbLocation(
                26.213001,
                19.195999,
                "Losal",
                "India",
                ""
            ),
            DbLocation(
                28.213001,
                39.195999,
                "Los Banos",
                "Brazil",
                ""
            ),
            DbLocation(
                18.213001,
                59.195999,
                "Los Angeles",
                "Use",
                "CA"
            ),
            DbLocation(
                10.213001,
                51.195999,
                "Los Angeles",
                "Philippines",
                ""
            ),
            DbLocation(
                13.213001,
                50.195999,
                "Los Angeles County",
                "Use",
                "CA"
            ),
            DbLocation(
                43.213001,
                40.195999,
                "Moscow",
                "Russia",
                ""
            ),
            DbLocation(
                29.213001,
                11.195999,
                "New York",
                "Use",
                "NY"
            ),

            DbLocation(
                13.213001,
                4.195999,
                "Paris",
                "France",
                ""
            )
        )
    }

    private class TestDispatcher() : Dispatcher() {
        override fun dispatch(request: RecordedRequest?): MockResponse {
            // Return 404 for not to force client to wait for timeout, and avoid
            // adding timeout time to test time
            return MockResponse().setResponseCode(404)
        }
    }
}