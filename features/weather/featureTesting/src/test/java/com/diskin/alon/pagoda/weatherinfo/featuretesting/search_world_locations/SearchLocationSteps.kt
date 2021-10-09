package com.diskin.alon.pagoda.weatherinfo.featuretesting.search_world_locations

import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsAdapter.LocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'User search for location' scenario.
 */
class SearchLocationSteps(db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val partialLocationQuery = "los"
    private val fullLocationName = "los angeles"
    private val locations = createDbLocations()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    init {
        // Prepare test db for scenario
        locations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}',0);"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @Given("^User open location search screen$")
    fun user_open_location_search_screen() {
        // Launch search fragment
        scenario = launchFragmentInHiltContainer<LocationsFragment>()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as LocationsFragment

            navController.setGraph(R.navigation.weather_graph)
            navController.setCurrentDestination(R.id.locationsFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^User search for location with partial query$")
    fun user_search_for_location_with_partial_query() {
        // Search for location with partial query
        onView(withId(R.id.action_search))
            .perform(ViewActions.click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(partialLocationQuery))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^All locations whose name starts with query should be shown$")
    fun all_locations_whose_name_starts_with_query_should_be_shown() {
        // Verify expected search results shown
        Thread.sleep(1000)
        checkUiLocationsShow(expectedUiLocationsPartialQuery())
    }

    @When("^User search for location with exact location name$")
    fun user_search_for_location_with_exact_location_name_something() {
        // Search for location with full location name query
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText(fullLocationName))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^All matching locations by name should be shown$")
    fun all_matching_locations_by_name_should_be_shown() {
        // Verify expected search results shown
        Thread.sleep(1000)
        checkUiLocationsShow(expectedUiLocationsFullLocationNameQuery())
    }

    private fun checkUiLocationsShow(locations: List<UiLocation>) {
        onView(withId(R.id.locations))
            .check(matches(isRecyclerViewItemsCount(locations.size)))

        locations.forEachIndexed { index, result ->
            onView(withId(R.id.locations))
                .perform(scrollToPosition<LocationViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.locations).atPositionOnView(index,R.id.location_name))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.locations).atPositionOnView(index,R.id.location_country))
                .check(matches(withText(result.country)))
        }
    }

    private data class DbLocation(val lat: Double,
                          val lon: Double,
                          val name: String,
                          val country: String,
                          val state: String)

    private data class UiLocation(val name: String,
                                  val country: String)

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
                "Usa",
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
                "Usa",
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
                "Usa",
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

    private fun expectedUiLocationsPartialQuery(): List<UiLocation> {
        val expectedUiLocations = mutableListOf<UiLocation>()

        locations.forEach {
            if (it.name.startsWith(partialLocationQuery,ignoreCase = true)) {
                expectedUiLocations.add(
                    UiLocation(
                        it.name,
                        it.country.plus(if (it.state.isEmpty()) "" else ", ".plus(it.state))
                    )
                )
            }
        }

        return expectedUiLocations
    }

    private fun expectedUiLocationsFullLocationNameQuery(): List<UiLocation> {
        val expectedUiLocations = mutableListOf<UiLocation>()

        locations.forEach {
            if (it.name.startsWith(fullLocationName,ignoreCase = true)) {
                expectedUiLocations.add(
                    UiLocation(
                        it.name,
                        it.country.plus(if (it.state.isEmpty()) "" else ", ".plus(it.state))
                    )
                )
            }
        }

        return expectedUiLocations
    }
}