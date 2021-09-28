package com.diskin.alon.pagoda.home.presentation

import android.os.Looper
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [MainActivity] hermetic ui test.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@MediumTest
@Config(sdk = [28],application = HiltTestApplication::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var scenario: ActivityScenario<MainActivity>

    // Collaborators
    @BindValue
    @JvmField
    val graphProvider: AppNavGraphProvider = mockk()

    @Before
    fun setUp() {
        // Stub collaborator
        every { graphProvider.getAppNavGraph() } returns getTestAppGraph()

        // Launch activity under test
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun openSettingsScreenWhenUserNavigates() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_settings,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.settingsFragment)
        }
    }

    @Test
    fun openWeatherScreenAsHomeWhenResumed() {
        // Given

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.weatherFragment)
        }
    }

    @Test
    fun showUpNavigationUiWhenSettingsScreenOpen() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_settings,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        onView(withContentDescription(R.string.abc_action_bar_up_description))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openSearchScreenWhenUserNavigates() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_search,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then location search should be shown
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.searchLocationsFragment)
        }
    }

    @Test
    fun openWeatherScreenWhenUserNavigates() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_current_location_weather,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.weatherFragment)
        }
    }

    @Test
    fun openBookmarksScreenWhenUserNavigates() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_bookmarks,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)
            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.bookmarkedLocationsFragment)
        }
    }

    @Test
    fun showUpNavigationUiWhenFavoriteLocationsScreenOpen() {
        // Given

        // When
        scenario.onActivity {
            val addMenuItem = ActionMenuItem(
                it,
                0,
                R.id.action_bookmarks,
                0,
                0,
                null
            )

            it.onOptionsItemSelected(addMenuItem)
        }

        // Then
        onView(withContentDescription(R.string.abc_action_bar_up_description))
            .check(matches(isDisplayed()))
    }
}