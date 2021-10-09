package com.diskin.alon.pagoda.weatherinfo.presentation

import android.content.Context
import android.os.Looper
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsAdapter
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationsFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.LocationsViewModel
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
 * [LocationsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class LocationsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: LocationsViewModel = mockk()

    // Stub data
    private val locations = MutableLiveData<PagingData<UiLocation>>()
    private val error = SingleLiveEvent<AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub collaborators
        every { viewModel.locations } returns locations
        every { viewModel.error } returns error
        every { viewModel getProperty "searchViewExpanded" } returns false
        every { viewModel setProperty "searchViewExpanded" value any<Boolean>() } just runs

        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageButton(any(),any()) } returns Unit

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<LocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showFavoriteLocations_WhenNotSearching() {
        // Given

        // When
        val favorites = createFavoriteLocations()
        locations.value = PagingData.from(favorites)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.locations)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(favorites.size)
        }

        favorites.forEachIndexed { index, result ->
            onView(withId(R.id.locations))
                .perform(scrollToPosition<LocationsAdapter.LocationViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.locations).atPositionOnView(index, R.id.location_name))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.locations).atPositionOnView(index, R.id.location_country))
                .check(matches(withText(result.country)))

            scenario.onActivity {
                val rv = it.findViewById<RecyclerView>(R.id.locations)
                val imageButton = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageButton>(R.id.favorite_button)
                verify { ImageLoader.loadIconResIntoImageButton(imageButton,R.drawable.ic_baseline_star_24) }
            }
        }
    }

    @Test
    fun searchWorldLocations_WhenNonEmptySearchQueryTyped() {
        // Given
        every { viewModel.search(any()) } returns Unit
        every { viewModel.loadFavorites() } returns Unit

        // When
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(typeText("xyz"))

        // Then
        verify { viewModel.search("x") }
        verify { viewModel.search("xy") }
        verify { viewModel.search("xyz") }
    }

    @Test
    fun loadFavorites_WhenEmptySearchQueryTyped() {
        // Given
        every { viewModel.loadFavorites() } returns Unit

        // When
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(typeText(""))

        // Then
        verify { viewModel.loadFavorites() }
    }

    @Test
    fun showSearchResults_WhenSearchDone() {
        // Given

        // When
        val searchResults = createSearchedLocations()
        locations.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.locations)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(searchResults.size)
        }

        searchResults.forEachIndexed { index, result ->
            onView(withId(R.id.locations))
                .perform(scrollToPosition<LocationsAdapter.LocationViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.locations).atPositionOnView(index, R.id.location_name))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.locations).atPositionOnView(index, R.id.location_country))
                .check(matches(withText(result.country)))

            scenario.onActivity {
                val rv = it.findViewById<RecyclerView>(R.id.locations)
                val imageButton = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageButton>(R.id.favorite_button)

                when(result.isFavorite) {
                    true -> verify { ImageLoader.loadIconResIntoImageButton(imageButton,R.drawable.ic_baseline_star_24) }
                    else -> verify { ImageLoader.loadIconResIntoImageButton(imageButton,R.drawable.ic_baseline_star_border_24) }
                }
            }
        }
    }

    @Test
    fun addLocationToFavorites_WhenUserFavoriteLocation() {
        // Given
        val location = createUnFavoriteLocation()

        locations.value = PagingData.from(listOf(location))
        every { viewModel.favoriteLocation(any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.favorite_button))
            .perform(click())

        // Then
        verify { viewModel.favoriteLocation(location) }
    }

    @Test
    fun deleteLocationFromFavorites_WhenUserUnfavoriteLocation() {
        // Given
        val location = createFavoritedLocation()

        locations.value = PagingData.from(listOf(location))
        every { viewModel.unfavoriteLocation(any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.favorite_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.unfavoriteLocation(location) }
    }

    @Test
    fun showLoadingProgress_WhenLocationsLoading() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as LocationsFragment
            fragment.handleLocationsLoadStates(
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
    fun hideLoadingProgress_WhenLocationsNotLoading() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as LocationsFragment
            fragment.handleLocationsLoadStates(
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
    fun showErrorMessage_WhenErrorOccur() {
        // Given
        val appError = AppError(ErrorType.DB_ERROR)

        // When
        error.value = appError
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.text_locations_db_error)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun restoreSearchViewState_WhenRecreatedFromSavedState() {
        // Given
        val query = "xyz"

        every { viewModel.search(any()) } returns Unit
        every { viewModel.query } returns query
        every { viewModel.searchViewExpanded } returns true
        every { viewModel.loadFavorites() } returns Unit

        // When
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.search_src_text))
            .perform(typeText(query))
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.search_src_text))
            .check(matches(withText(query)))
    }
}