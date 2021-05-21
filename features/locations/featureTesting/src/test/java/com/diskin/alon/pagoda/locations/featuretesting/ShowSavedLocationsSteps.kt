package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsAdapter
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsFragment
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.robolectric.Shadows

/**
 * Step definitions for 'Saved locations listed' scenario.
 */
class ShowSavedLocationsSteps(
    private val db: TestDatabase,
    navProvider: AppLocationsNavProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val bookmarkedLocations = createDbBookmarkedLocations()
    private val worldLocations = createDbLocations()
    private val weatherDataDetId = 10
    private val bundleSlot = slot<Bundle>()

    init {
        // Prepare nav dest provider
        every { navProvider.getWeatherDest() } returns weatherDataDetId

        // Prepare nav controller
        mockkStatic(Fragment::findNavController)
    }

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

        // Stub mocked nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            every { fragment.findNavController().navigate(any(),capture(bundleSlot)) } returns Unit
        }
    }

    @Then("^All saved locations are listed in descending added order$")
    fun all_saved_locations_are_listed_in_descending_added_order() {
        val uiLocations = expectedUiLocations()

        onView(withId(R.id.saved_locations))
            .check(matches(isRecyclerViewItemsCount(uiLocations.size)))

        uiLocations.forEachIndexed { index, location ->
            onView(withId(R.id.saved_locations))
                .perform(scrollToPosition<SavedLocationsAdapter.SavedLocationViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index,R.id.locationName))
                .check(matches(withText(location.name)))

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index,R.id.locationCountry))
                .check(matches(withText(location.country)))
        }
    }

    @When("^User select first listed location$")
    fun user_select_first_listed_location() {
        onView(withId(R.id.saved_locations))
            .perform(
                actionOnItemAtPosition<SavedLocationsAdapter.SavedLocationViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^App should show selected location weather data$")
    fun app_should_show_selected_location_weather_data() {
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            verify { fragment.findNavController().navigate(weatherDataDetId,any()) }
        }

        assertThat(bundleSlot.captured.get(LOCATION_LAT)).isEqualTo(bookmarkedLocations.first().lat)
        assertThat(bundleSlot.captured.get(LOCATION_LON)).isEqualTo(bookmarkedLocations.first().lon)
    }

    private fun expectedUiLocations(): List<UiLocation> {
        val expectedUiLocations = mutableListOf<UiLocation>()

        worldLocations.forEach {
            if (isWorldLocationBookmarked(it)) {
                expectedUiLocations.add(
                    UiLocation(
                        it.name,
                        it.country.plus(if (it.state.isEmpty()) "" else ", ".plus(it.state))
                    )
                )
            }
        }

        return expectedUiLocations.reversed()
    }

    private fun isWorldLocationBookmarked(location: DbLocation): Boolean {
        bookmarkedLocations.forEach {
            if (it.lat == location.lat && it.lon == location.lon)
                return true
        }

        return false
    }

    private data class DbLocation(val lat: Double,
                                  val lon: Double,
                                  val name: String,
                                  val country: String,
                                  val state: String)

    private data class DbBookmarkedLocation(val lat: Double, val lon: Double)

    private data class UiLocation(val name: String,
                                  val country: String)

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

    private fun createDbBookmarkedLocations() = listOf(
        DbBookmarkedLocation(
            13.213001,
            4.195999
        ),
        DbBookmarkedLocation(
            43.213001,
            40.195999
        )
    )
}