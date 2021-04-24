package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
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
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.SearchLocationFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'Search result location weather shown' scenario.
 */
class ShowLocationResultWeatherSteps(db: TestDatabase) : GreenCoffeeSteps() {
    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val lat = 23.45
    private val lon = 53.45
    private val query = "london"

    init {
        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state)" +
                "VALUES(${lat},${lon},'${query}','country','state');"

        db.compileStatement(insertSql).executeInsert()

        // Prepare test nav controller
        navController.setGraph(R.navigation.test_locations_nav_graph)
        navController.setCurrentDestination(R.id.searchLocationFragment)
    }

    @Given("^User open location search screen$")
    fun user_open_location_search_screen(){
        // Launch search locations fragment
        scenario = launchFragmentInHiltContainer<SearchLocationFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
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
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.testWeatherInfoFragment)
        assertThat(navController.currentBackStackEntry?.arguments?.get(LOCATION_LAT))
            .isEqualTo(lat)
        assertThat(navController.currentBackStackEntry?.arguments?.get(LOCATION_LON))
            .isEqualTo(lon)
    }
}