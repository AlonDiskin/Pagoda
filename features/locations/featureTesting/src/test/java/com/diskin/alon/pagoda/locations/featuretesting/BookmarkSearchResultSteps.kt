package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsFragment
import com.diskin.alon.pagoda.locations.presentation.controller.SearchLocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'User bookmark location result' scenario.
 */
class BookmarkSearchResultSteps(private val db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val query = "los angeles"
    private val locations = createDbLocations()

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
        scenario = launchFragmentInHiltContainer<SearchLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        scenario.onActivity {
            // Disable PagingDataAdapter animation for testing
            it.findViewById<RecyclerView>(R.id.search_location_results).itemAnimator = null
        }
    }

    @When("^User search for location$")
    fun user_search_for_location() {
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^Select to bookmark first result$")
    fun select_to_bookmark_first_result() {
        Thread.sleep(3000)
        onView(withId(R.id.add_bookmark_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should bookmark select location$")
    fun app_should_bookmark_select_location() {
        val bookmarked = db.compileStatement("SELECT COUNT(*) FROM locations WHERE bookmarked = 1")
            .simpleQueryForLong()

        assertThat(bookmarked).isEqualTo(1)
    }

    @When("^User open bookmarked locations screen$")
    fun user_open_bookmarked_locations_screen() {
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        scenario.onActivity {
            // Disable PagingDataAdapter animation for testing
            it.findViewById<RecyclerView>(R.id.bookmarked_locations).itemAnimator = null
        }
    }

    @Then("^Bookmarked location should be listed$")
    fun bookmarked_location_should_be_listed() {
        Thread.sleep(3000)
        val bookmarkedLocation = expectedBookmarkedUiLocation()

        onView(withId(R.id.bookmarked_locations))
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