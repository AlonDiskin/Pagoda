package com.diskin.alon.pagoda.locations.presentation

import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsAdapter
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsAdapter.SavedLocationViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.SavedLocationsFragment
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SavedLocationsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

/**
 * [SavedLocationsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class SavedLocationsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: SavedLocationsViewModel = mockk()
    private val appNav: AppLocationsNavProvider = mockk()

    // Stub data
    private val locations = MutableLiveData<PagingData<UiLocation>>()
    private val error = SingleLiveEvent<AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.locations } returns locations
        every { viewModel.error } returns error

        // Launch fragment under test

        // Fragment scenario has no action bar that needed for test so we use hilt
        // test activity(uses activity scenario)
        scenario = launchFragmentInHiltContainer<SavedLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showProgressBarWhenLoadingLocations() {
        // Given

        // When
        scenario.onActivity {
            val rv = it.findViewById<RecyclerView>(R.id.saved_locations)
            val listener = getSearchResultsAdapterLoadStatesListener(
                rv.adapter as SavedLocationsAdapter)

            listener.invoke(
                CombinedLoadStates(
                    LoadState.Loading,
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadStates(
                        LoadState.Loading,
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true)
                    )
                )
            )
        }

        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // Then
        onView(withId(R.id.progress_bar))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun hideProgressBarWhenSavedLocationsLoaded() {
        // Given

        // When
        scenario.onActivity {
            val rv = it.findViewById<RecyclerView>(R.id.saved_locations)
            val listener = getSearchResultsAdapterLoadStatesListener(
                rv.adapter as SavedLocationsAdapter
            )

            listener.invoke(
                CombinedLoadStates(
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadStates(
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true)
                    )
                )
            )
        }

        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // Then
        onView(withId(R.id.progress_bar))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun showSavedLocationsWhenLoaded() {
        // Test case fixture
        scenario.onActivity { it.findViewById<RecyclerView>(R.id.saved_locations).itemAnimator = null }

        // Given

        // When
        val savedLocations = createSavedLocations()
        locations.value = PagingData.from(savedLocations)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.saved_locations)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(savedLocations.size)
        }

        savedLocations.forEachIndexed { index, result ->
            onView(withId(R.id.saved_locations))
                .perform(scrollToPosition<SavedLocationViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index, R.id.locationName))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.saved_locations).atPositionOnView(index, R.id.locationCountry))
                .check(matches(withText(result.country)))
        }
    }

    @Test
    fun openLocationsSearchScreenWhenFabSelected() {
        // Test case fixture
        val destId = 10

        mockkStatic(Fragment::findNavController)
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            fragment.appNav = appNav
            every { fragment.findNavController().navigate(any<Int>()) } returns Unit
        }
        every { appNav.getLocationsSearchDest() } returns destId

        // Given

        // When
        onView(withId(R.id.add_fab))
            .perform(click())

        // Then
        verify { appNav.getLocationsSearchDest() }

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            verify { fragment.findNavController().navigate(destId) }
        }
    }

    @Test
    fun openWeatherDataScreenWhenResultSelected() {
        // Test case fixture
        val destId = 10
        val bundleSlot = slot<Bundle>()
        mockkStatic(Fragment::findNavController)

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            fragment.appNav = appNav
            every { fragment.findNavController().navigate(any(),capture(bundleSlot)) } returns Unit
        }

        every { appNav.getWeatherDest() } returns destId

        // Given

        // When
        val savedLocations = createSavedLocations()
        locations.value = PagingData.from(savedLocations)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // And
        onView(withId(R.id.saved_locations))
            .perform(actionOnItemAtPosition<SavedLocationViewHolder>(0, click()))

        // Then
        verify { appNav.getWeatherDest() }

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SavedLocationsFragment
            verify { fragment.findNavController().navigate(destId,any()) }
        }

        assertThat(bundleSlot.captured.get(LOCATION_LAT)).isEqualTo(savedLocations.first().lat)
        assertThat(bundleSlot.captured.get(LOCATION_LON)).isEqualTo(savedLocations.first().lon)
    }

    @Test
    fun deleteLocationUponUserSelection() {
        // Test case fixture
        every { viewModel.deleteSavedLocation(any()) } returns Unit

        // Given
        val savedLocations = createSavedLocations()
        locations.value = PagingData.from(listOf(savedLocations.first()))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.locationOptions))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_location_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val dialog = (ShadowAlertDialog.getLatestDialog() as AlertDialog)
        assertThat(dialog.isShowing).isTrue()

        // When
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.deleteSavedLocation(savedLocations.first()) }
    }

    @Test
    fun notifyUserWhenDeleteLocationFail() {
        // Given

        // When
        error.value = AppError(ErrorType.DB_ERROR)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.text_locations_db_error)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }
}