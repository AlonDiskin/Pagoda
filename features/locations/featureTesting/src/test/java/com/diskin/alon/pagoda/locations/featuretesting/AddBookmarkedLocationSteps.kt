package com.diskin.alon.pagoda.locations.featuretesting

import android.os.Looper
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ActivityScenario
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.BookmarkedLocationsFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.robolectric.Shadows

/**
 * Step definitions for 'User add bookmarked location' scenario.
 */
class AddBookmarkedLocationSteps (navProvider: AppLocationsNavProvider): GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val locationsSearchDetId = 10

    init {
        // Prepare nav dest provider
        every { navProvider.getBookmarkedLocationsLocationsSearchNavRoute() } returns locationsSearchDetId

        // Prepare nav controller
        mockkStatic(Fragment::findNavController)
    }

    @Given("^User open saved locations screen$")
    fun user_open_saved_locations_screen() {
        // Launch locations fragment
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // Stub mocked nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as BookmarkedLocationsFragment
            every { fragment.findNavController().navigate(any<Int>()) } returns Unit
        }
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
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as BookmarkedLocationsFragment
            verify { fragment.findNavController().navigate(locationsSearchDetId) }
        }
    }
}