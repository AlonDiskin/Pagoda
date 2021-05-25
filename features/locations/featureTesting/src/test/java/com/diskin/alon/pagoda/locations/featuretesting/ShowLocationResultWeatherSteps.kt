package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Bundle
import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.SearchLocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.robolectric.Shadows

/**
 * Step definitions for 'Search result location weather shown' scenario.
 */
class ShowLocationResultWeatherSteps(
    db: TestDatabase,
    navProvider: AppLocationsNavProvider
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val lat = 23.45
    private val lon = 53.45
    private val query = "london"
    private val weatherDataDetId = 10
    private val bundleSlot = slot<Bundle>()

    init {
        // Prepare nav dest provider
        every { navProvider.getWeatherDest() } returns weatherDataDetId

        // Prepare nav controller
        mockkStatic(Fragment::findNavController)

        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state)" +
                "VALUES(${lat},${lon},'${query}','country','state');"

        db.compileStatement(insertSql).executeInsert()
    }

    @Given("^User open location search screen$")
    fun user_open_location_search_screen(){
        // Launch search locations fragment
        scenario = launchFragmentInHiltContainer<SearchLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Stub mocked nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            every { fragment.findNavController().navigate(any(),capture(bundleSlot)) } returns Unit
        }
    }

    @When("^User search for location$")
    fun user_search_for_location(){
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^Select the first search result$")
    fun select_the_first_search_result(){
        Thread.sleep(1000)
        onView(withId(R.id.searchResults))
            .perform(
                actionOnItemAtPosition<LocationSearchResultViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^Selected location weather should be shown in weather data screen$")
    fun selected_location_weather_should_be_shown_in_weather_data_screen(){
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            verify { fragment.findNavController().navigate(weatherDataDetId,any()) }
        }

        assertThat(bundleSlot.captured.get(LOCATION_LAT)).isEqualTo(lat)
        assertThat(bundleSlot.captured.get(LOCATION_LON)).isEqualTo(lon)
    }
}