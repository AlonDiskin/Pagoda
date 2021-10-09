package com.diskin.alon.pagoda.weatherinfo.featuretesting.search_world_locations

import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'User favorite location search result' scenario.
 */
class FavoriteSearchResultSteps(private val db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val query = "los angeles"
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

    @Given("^User open locations search screen$")
    fun user_open_locations_search_screen() {
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

    @When("^User search for location$")
    fun user_search_for_location() {
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^Select to favorite first result$")
    fun select_to_favorite_first_result() {
        Thread.sleep(3000)
        onView(withId(R.id.favorite_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should favorite select location$")
    fun app_should_favorite_select_location() {
        val bookmarked = db.compileStatement("SELECT COUNT(*) FROM locations WHERE bookmarked = 1")
            .simpleQueryForLong()

        assertThat(bookmarked).isEqualTo(1)
    }

    @When("^User open favorite locations screen$")
    fun user_open_bookmarked_locations_screen() {
        onView(withId(R.id.search_close_btn))
            .perform(click())
    }

    @Then("^Favorite location should be listed$")
    fun favorite_location_should_be_listed() {
        Thread.sleep(3000)
        val bookmarkedLocation = expectedBookmarkedUiLocation()

        onView(withId(R.id.locations))
            .check(matches(isRecyclerViewItemsCount(1)))

        onView(withId(R.id.location_name))
            .check(matches(withText(bookmarkedLocation.name)))

        onView(withId(R.id.location_country))
            .check(matches(withText(bookmarkedLocation.country)))
    }

    private fun expectedBookmarkedUiLocation(): UiLocation {
        return UiLocation("Los Angeles","Usa, CA")
    }

    private data class UiLocation(val name: String,
                                  val country: String)

    private data class DbLocation(val lat: Double,
                                  val lon: Double,
                                  val name: String,
                                  val country: String,
                                  val state: String)

    private fun createDbLocations(): List<DbLocation> {
        return listOf(
            DbLocation(
                18.213001,
                59.195999,
                "Los Angeles",
                "Usa",
                "CA"
            )
        )
    }
}