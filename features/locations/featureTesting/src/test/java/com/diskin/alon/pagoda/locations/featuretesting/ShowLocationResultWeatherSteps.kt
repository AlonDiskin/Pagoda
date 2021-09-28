package com.diskin.alon.pagoda.locations.featuretesting

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.pagoda.common.presentation.ARG_LAT
import com.diskin.alon.pagoda.common.presentation.ARG_LON
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.SearchLocationsFragment
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
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val lat = 23.45
    private val lon = 53.45
    private val query = "london"

    init {
        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                "VALUES(${lat},${lon},'${query}','country','state',0);"

        db.compileStatement(insertSql).executeInsert()
    }

    @Given("^User open location search screen$")
    fun user_open_location_search_screen(){
        // Launch search locations fragment
        scenario = launchFragmentInHiltContainer<SearchLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as SearchLocationsFragment

            navController.setGraph(R.navigation.locations_graph)
            navController.graph.addDestination(NavDestination("test navigator").also { dest ->
                dest.id = 10
                dest.addDeepLink(it.getString(R.string.uri_weather))
            })
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
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
        onView(withId(R.id.search_location_results))
            .perform(
                actionOnItemAtPosition<LocationSearchResultViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^Selected location weather should be shown in weather data screen$")
    fun selected_location_weather_should_be_shown_in_weather_data_screen(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedDeepLinkUri = Uri.Builder()
            .scheme(context.getString(R.string.uri_weather).toUri().scheme)
            .authority(context.getString(R.string.uri_weather).toUri().authority)
            .path(context.getString(R.string.uri_weather).toUri().path)
            .appendQueryParameter(ARG_LAT,lat.toString())
            .appendQueryParameter(ARG_LON,lon.toString())
            .build()

        assertThat(navController.currentDestination?.hasDeepLink(expectedDeepLinkUri)).isTrue()
    }
}