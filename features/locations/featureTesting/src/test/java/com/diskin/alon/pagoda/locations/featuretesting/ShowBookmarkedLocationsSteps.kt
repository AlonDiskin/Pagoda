package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
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
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsAdapter.BookmarkedLocationViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsFragment
import com.google.common.truth.Truth.assertThat
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
 * Step definitions for 'Bookmarked locations listed' scenario.
 */
class ShowBookmarkedLocationsSteps(
    private val db: TestDatabase,
    navProvider: AppLocationsNavProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val bookmarkedLocations = createDbBookmarkedLocations()
    private val weatherDataDetId = 10
    private val bundleSlot = slot<Bundle>()

    init {
        // Prepare nav dest provider
        every { navProvider.getBookmarkedLocationsToWeatherDataNavRoute() } returns weatherDataDetId

        // Prepare nav controller
        mockkStatic(Fragment::findNavController)
    }

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
        // Launch bookmarked locations fragment
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // Stub mocked nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as BookmarkedLocationsFragment
            every { fragment.findNavController().navigate(any(),capture(bundleSlot)) } returns Unit
        }
    }

    @Then("^All bookmarked locations are listed in descending added order$")
    fun all_bookmarked_locations_are_listed_in_descending_added_order() {
        val uiLocations = expectedUiLocations()

        onView(withId(R.id.bookmarked_locations))
            .check(matches(isRecyclerViewItemsCount(uiLocations.size)))

        uiLocations.forEachIndexed { index, location ->
            onView(withId(R.id.bookmarked_locations))
                .perform(scrollToPosition<BookmarkedLocationViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index,R.id.location_name))
                .check(matches(withText(location.name)))

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index,R.id.location_country))
                .check(matches(withText(location.country)))
        }
    }

    @When("^User select first listed location$")
    fun user_select_first_listed_location() {
        onView(withId(R.id.bookmarked_locations))
            .perform(
                actionOnItemAtPosition<BookmarkedLocationViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^App should show selected location weather data$")
    fun app_should_show_selected_location_weather_data() {
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as BookmarkedLocationsFragment
            verify { fragment.findNavController().navigate(weatherDataDetId,any()) }
        }

        assertThat(bundleSlot.captured.get(LOCATION_LAT)).isEqualTo(bookmarkedLocations.first().lat)
        assertThat(bundleSlot.captured.get(LOCATION_LON)).isEqualTo(bookmarkedLocations.first().lon)
    }

    private fun expectedUiLocations(): List<UiLocation> {
        val expectedUiLocations = mutableListOf<UiLocation>()

        bookmarkedLocations.forEach {
            expectedUiLocations.add(
                UiLocation(
                    it.name,
                    it.country.plus(if (it.state.isEmpty()) "" else ", ".plus(it.state))
                )
            )
        }

        return expectedUiLocations
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
        ),
        DbBookmarkedLocation(
            18.213001,
            59.195999,
            "Los Angeles",
            "Use",
            "CA"
        )
    )
}