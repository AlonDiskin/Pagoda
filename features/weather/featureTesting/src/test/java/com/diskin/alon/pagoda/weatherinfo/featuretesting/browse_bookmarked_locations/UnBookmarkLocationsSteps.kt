package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_bookmarked_locations

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.BookmarkedLocationsAdapter.BookmarkedLocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.BookmarkedLocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

/**
 * Step definitions for 'User unbookmark locations' scenario.
 */
class UnBookmarkLocationsSteps(private val db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val bookmarkedLocations = createDbBookmarkedLocations()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Given("^Previously bookmarked locations exist$")
    fun previously_bookmarked_locations_exist() {
        bookmarkedLocations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}',1);"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @When("^User open bookmarked locations screen$")
    fun user_open_bookmarked_locations_screen() {
        // Launch saved locations fragment
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as BookmarkedLocationsFragment

            navController.setGraph(R.navigation.weather_graph)
            navController.setCurrentDestination(R.id.bookmarkedLocationsFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        Thread.sleep(2000)
    }

    @And("^Selects to unbookmark first and last listed locations$")
    fun selects_to_unbookmark_first_and_last_listed_locations() {
        // Select to unbookmark first shown location
        onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(0,R.id.locationOptions))
            .perform(click())

        onView(withText(R.string.title_action_location_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Select to unbookmark last shown location
        onView(withRecyclerView(R.id.bookmarked_locations)
            .atPositionOnView(bookmarkedLocations.size - 1,R.id.locationOptions))
            .perform(click())

        onView(withText(R.string.title_action_location_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
    }

    @Then("^App should unbookmark locations$")
    fun app_should_unbookmark_locations() {
        val actualBookmarked = db.compileStatement("SELECT COUNT(*) FROM locations WHERE bookmarked").simpleQueryForLong()
        assertThat(actualBookmarked).isEqualTo(bookmarkedLocations.size - 2)
    }

    @And("^App should update shown list$")
    fun app_should_update_shown_list() {
        val uiLocations = expectedPostUnBookmarkUiLocations()

        onView(withId(R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(uiLocations.size)))

        uiLocations.forEachIndexed { index, location ->
            onView(withId(R.id.bookmarked_locations))
                .perform(
                    scrollToPosition<BookmarkedLocationViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index,R.id.location_name))
                .check(matches(withText(location.name)))

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index,R.id.location_country))
                .check(matches(withText(location.country)))
        }
    }

    private fun expectedPostUnBookmarkUiLocations(): List<UiLocation> {
        return listOf(UiLocation(
            "Losal",
            "India",
        ))
    }

    private data class DbBookmarkedLocation(val lat: Double,
                                            val lon: Double,
                                            val name: String,
                                            val country: String,
                                            val state: String)

    private data class UiLocation(val name: String,
                                  val country: String)

    private fun createDbBookmarkedLocations() = listOf(
        DbBookmarkedLocation(
            36.213001,
            49.195999,
            "Losevo",
            "Russia",
            ""
        ),
        DbBookmarkedLocation(
            26.213001,
            19.195999,
            "Losal",
            "India",
            ""
        ),
        DbBookmarkedLocation(
            28.213001,
            39.195999,
            "Los Banos",
            "Brazil",
            ""
        )
    )
}