package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_favorite_locations

import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsAdapter.LocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'Favorite locations listed' scenario.
 */
class ShowFavoriteLocationsSteps(private val db: TestDatabase) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val bookmarkedLocations = createDbBookmarkedLocations()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val fragmentResultListener = object : FragmentResultListener {
        var bundle: Bundle? = null
        override fun onFragmentResult(requestKey: String, result: Bundle) {
            bundle = result
        }
    }

    @Given("^Previously favorite locations exist$")
    fun previously_favorite_locations_exist() {
        bookmarkedLocations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}',1);"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @When("^User open favorite locations screen$")
    fun user_open_favorite_locations_screen() {
        // Launch fragment
        scenario = launchFragmentInHiltContainer<LocationsFragment>()

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as LocationsFragment

            // Set test nav controller
            navController.setGraph(R.navigation.weather_graph)
            navController.setCurrentDestination(R.id.locationsFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)

            // Set fragment result listener
            it.supportFragmentManager.setFragmentResultListener(
                it.getString(R.string.locaiton_request_key),
                it,
                fragmentResultListener
            )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
    }

    @Then("^All favorite locations are listed in descending added order$")
    fun all_favorite_locations_are_listed_in_descending_added_order() {
        val uiLocations = expectedUiLocations()

        onView(withId(R.id.locations))
            .check(matches(isRecyclerViewItemsCount(uiLocations.size)))

        uiLocations.forEachIndexed { index, location ->
            onView(withId(R.id.locations))
                .perform(scrollToPosition<LocationViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.locations).atPositionOnView(index,R.id.location_name))
                .check(matches(withText(location.name)))

            onView(withRecyclerView(R.id.locations).atPositionOnView(index,R.id.location_country))
                .check(matches(withText(location.country)))
        }
    }

    @When("^User select first listed location$")
    fun user_select_first_listed_location() {
        onView(withId(R.id.locations))
            .perform(
                actionOnItemAtPosition<LocationViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^App should show selected location weather data$")
    fun app_should_show_selected_location_weather_data() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.weatherFragment)
        assertThat(fragmentResultListener.bundle?.getDouble(context.getString(R.string.arg_lat_key)))
            .isEqualTo(bookmarkedLocations.first().lat)
        assertThat(fragmentResultListener.bundle?.getDouble(context.getString(R.string.arg_lon_key)))
            .isEqualTo(bookmarkedLocations.first().lon)
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