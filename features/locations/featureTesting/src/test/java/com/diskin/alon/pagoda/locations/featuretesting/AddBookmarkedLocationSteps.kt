package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Looper
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

/**
 * Step definitions for 'User add bookmarked location' scenario.
 */
class AddBookmarkedLocationSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Given("^User open saved locations screen$")
    fun user_open_saved_locations_screen() {
        // Launch locations fragment
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as BookmarkedLocationsFragment

            navController.setGraph(R.navigation.locations_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^User select to add a new saved location$")
    fun user_select_to_add_a_new_saved_location() {
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as BookmarkedLocationsFragment
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_add,
                0,
                0,
                null
            )

            fragment.onOptionsItemSelected(addMenuItem)
        }
    }

    @Then("^App should open world location search screen$")
    fun app_should_open_world_location_search_screen() {
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.searchLocationsFragment)
    }
}