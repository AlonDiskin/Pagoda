package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
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
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsAdapter
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

/**
 * Step definitions for 'User delete saved location' scenario.
 */
class DeleteSavedLocationSteps(private val db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val bookmarkedLocations = createDbBookmarkedLocations()
    private val worldLocations = createDbLocations()

    @Given("^Previously saved locations exist$")
    fun previously_saved_locations_exist() {
        worldLocations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}');"

            db.compileStatement(insertSql).executeInsert()
        }

        bookmarkedLocations.forEach {
            val insertSql = "INSERT INTO bookmarked_locations (lat,lon)" +
                    "VALUES(${it.lat},${it.lon});"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @When("^User open saved locations screen$")
    fun user_open_saved_locations_screen() {
        // Launch saved locations fragment
        scenario = launchFragmentInHiltContainer<SavedLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
    }

    @And("^Selects to remove first and last listed locations$")
    fun selects_to_remove_first_and_last_listed_locations() {
        // Select to remove first shown location
        onView(withRecyclerView(R.id.saved_locations).atPositionOnView(0,R.id.locationOptions))
            .perform(click())

        onView(withText(R.string.title_action_location_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Select to remove last shown location
        onView(withRecyclerView(R.id.saved_locations)
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

    @Then("^App should remove locations from user saved locations$")
    fun app_should_remove_locations_from_user_saved_locations() {
        val actualBookmarked = db.compileStatement("SELECT COUNT(*) FROM bookmarked_locations").simpleQueryForLong()
        assertThat(actualBookmarked).isEqualTo(bookmarkedLocations.size - 2)
    }

    @And("^App should update shown list$")
    fun app_should_update_shown_list() {
        val uiLocations = expectedPostDeleteUiLocations()

        onView(withId(R.id.saved_locations))
            .check(matches(isRecyclerViewItemsCount(uiLocations.size)))

        uiLocations.forEachIndexed { index, location ->
            onView(withId(R.id.saved_locations))
                .perform(
                    scrollToPosition<SavedLocationsAdapter.SavedLocationViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index,R.id.locationName))
                .check(matches(withText(location.name)))

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index,R.id.locationCountry))
                .check(matches(withText(location.country)))
        }
    }

    private fun expectedPostDeleteUiLocations(): List<UiLocation> {
        return listOf(
            UiLocation(
                "Losal",
                "India"
            )
        )
    }

    private data class UiLocation(val name: String,
                                  val country: String)

    private data class DbLocation(val lat: Double,
                                  val lon: Double,
                                  val name: String,
                                  val country: String,
                                  val state: String)

    private data class DbBookmarkedLocation(val lat: Double, val lon: Double)

    private fun createDbLocations() = listOf(
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
        )
    )

    private fun createDbBookmarkedLocations() = listOf(
        DbBookmarkedLocation(
            36.213001,
            49.195999
        ),
        DbBookmarkedLocation(
            26.213001,
            19.195999
        ),
        DbBookmarkedLocation(
            28.213001,
            39.195999,
        )
    )
}