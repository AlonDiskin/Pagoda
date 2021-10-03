package com.diskin.alon.pagoda.weatherinfo.presentation

import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
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
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.BookmarkedLocationsAdapter.BookmarkedLocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.BookmarkedLocationsFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiBookmarkedLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.BookmarkedLocationsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

/**
 * [BookmarkedLocationsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class BookmarkedLocationsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: BookmarkedLocationsViewModel = mockk()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val locations = MutableLiveData<PagingData<UiBookmarkedLocation>>()
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
        scenario = launchFragmentInHiltContainer<BookmarkedLocationsFragment>()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as BookmarkedLocationsFragment

            navController.setGraph(R.navigation.weather_graph)
            navController.setCurrentDestination(R.id.bookmarkedLocationsFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showBookmarksTitleInAppBar_WhenResumed() {
        // Given

        // Then
        val expectedTitle = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.label_bookmarks_fragment)
        assertThat(navController.currentDestination?.label).isEqualTo(expectedTitle)
    }

    @Test
    fun showProgressBar_WhenLoadingLocations() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as BookmarkedLocationsFragment
            fragment.handleBookmarksLoadStates(
                CombinedLoadStates(
                    LoadState.Loading,
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
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun hideProgressBar_WhenSavedLocationsLoaded() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as BookmarkedLocationsFragment
            fragment.handleBookmarksLoadStates(
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
    fun showSavedLocations_WhenLoaded() {
        // Given

        // When
        val savedLocations = createBookmarkedLocations()
        locations.value = PagingData.from(savedLocations)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.bookmarked_locations)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(savedLocations.size)
        }

        savedLocations.forEachIndexed { index, result ->
            onView(withId(R.id.bookmarked_locations))
                .perform(scrollToPosition<BookmarkedLocationViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index, R.id.location_name))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.bookmarked_locations).atPositionOnView(index, R.id.location_country))
                .check(matches(withText(result.country)))
        }
    }

    @Test
    fun openWeatherDataScreen_WhenResultSelected() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val savedLocations = createBookmarkedLocations()
        locations.value = PagingData.from(savedLocations)
        var bundle: Bundle? = null

        scenario.onActivity {
            val listener = FragmentResultListener { _, result -> bundle = result }
            it.supportFragmentManager.setFragmentResultListener(
                context.getString(R.string.locaiton_request_key),
                it,
                listener
            )
        }

        // When
        onView(withId(R.id.bookmarked_locations))
            .perform(actionOnItemAtPosition<BookmarkedLocationViewHolder>(0, click()))

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.weatherFragment)
        assertThat(bundle?.getDouble(context.getString(R.string.arg_lat_key))).isEqualTo(savedLocations.first().lat)
        assertThat(bundle?.getDouble(context.getString(R.string.arg_lon_key))).isEqualTo(savedLocations.first().lon)
    }

    @Test
    fun deleteLocationUponUserSelection() {
        // Given
        val locations = createBookmarkedLocations()
        this.locations.value = PagingData.from(listOf(locations.first()))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        every { viewModel.deleteSavedLocation(any()) } returns Unit

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
        verify { viewModel.deleteSavedLocation(locations.first()) }
    }

    @Test
    fun notifyUser_WhenDeleteLocationFail() {
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