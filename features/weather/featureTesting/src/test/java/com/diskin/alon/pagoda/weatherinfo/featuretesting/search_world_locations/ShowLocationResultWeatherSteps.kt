package com.diskin.alon.pagoda.weatherinfo.featuretesting.search_world_locations

import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsAdapter.LocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsFragment
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
    private val fragmentResultListener = object : FragmentResultListener {
        var bundle: Bundle? = null
        override fun onFragmentResult(requestKey: String, result: Bundle) {
            bundle = result
        }
    }

    init {
        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                "VALUES(${lat},${lon},'${query}','country','state',0);"

        db.compileStatement(insertSql).executeInsert()
    }

    @Given("^User open location search screen$")
    fun user_open_location_search_screen(){
        // Launch search locations fragment
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
    }

    @When("^User search for location$")
    fun user_search_for_location(){
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^Select the first search result$")
    fun select_the_first_search_result(){
        Thread.sleep(1000)
        onView(withId(R.id.locations))
            .perform(
                actionOnItemAtPosition<LocationViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^Selected location weather should be shown in weather data screen$")
    fun selected_location_weather_should_be_shown_in_weather_data_screen(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.weatherFragment)
        assertThat(fragmentResultListener.bundle?.getDouble(context.getString(R.string.arg_lat_key)))
            .isEqualTo(lat)
        assertThat(fragmentResultListener.bundle?.getDouble(context.getString(R.string.arg_lon_key)))
            .isEqualTo(lon)
    }
}