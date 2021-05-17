package com.diskin.alon.pagoda.locations.presentation

import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.widget.SearchView
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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withSearchViewHint
import com.diskin.alon.pagoda.common.uitesting.withSearchViewQuery
import com.diskin.alon.pagoda.locations.presentation.controller.*
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SearchLocationsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [SearchLocationsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class SearchLocationsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: SearchLocationsViewModel = mockk()
    private val appNav: AppLocationsNavProvider = mockk()

    // Stub data
    private val results = MutableLiveData<PagingData<UiLocation>>()
    private val query = "query"

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.query } returns query
        every { viewModel.results } returns results

        // Launch fragment under test

        // Fragment scenario has no action bar that needed for test so we use hilt
        // test activity(uses activity scenario)
        scenario = launchFragmentInHiltContainer<SearchLocationsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun expandSearchInputWhenResumed() {
        // Given a resumed fragment

        // Then
        onView(instanceOf(SearchView::class.java))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun provideSearchHintForSearchQueryInput() {
        // Given a resumed fragment

        // Then
        onView(instanceOf(SearchView::class.java))
            .check(matches(withSearchViewHint(R.string.search_hint)))
    }

    @Test
    fun showCurrentSearchQueryWhenResumed() {
        // Given a resumed fragment

        // Then
        onView(instanceOf(SearchView::class.java))
            .check(matches(withSearchViewQuery(query)))

        // And
        verify(exactly = 0) { viewModel.search(any()) }
    }

    @Test
    fun searchLocationsWhenQueryInputChange() {
        // Test case fixture
        every { viewModel.search(any()) } returns Unit

        // Given a resumed fragment

        // When
        onView(withId(R.id.search_src_text))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        onView(withId(R.id.search_src_text))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

        // Then
        verify { viewModel.search("quer") }
        verify { viewModel.search("que") }
    }

    @Test
    fun showProgressBarWhenSearching() {
        // Given a resumed fragment

        // When
        scenario.onActivity {
            val rv = it.findViewById<RecyclerView>(R.id.searchResults)
            val listener = getSearchResultsAdapterLoadStatesListener(
                rv.adapter as LocationSearchResultsAdapter
            )

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
    fun hideProgressBarWhenResultsAvailable() {
        // Given a resumed fragment

        // When
        scenario.onActivity {
            val rv = it.findViewById<RecyclerView>(R.id.searchResults)
            val listener = getSearchResultsAdapterLoadStatesListener(
                rv.adapter as LocationSearchResultsAdapter
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
    fun showSearchResultsWhenResultsAvailable() {
        // Test case fixture
        scenario.onActivity { it.findViewById<RecyclerView>(R.id.searchResults).itemAnimator = null }

        // Given a resumed fragment

        // When
        val searchResults = createSearchResults()
        results.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.searchResults)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(searchResults.size)
        }

        searchResults.forEachIndexed { index, result ->
            onView(withId(R.id.searchResults))
                .perform(scrollToPosition<LocationSearchResultViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.searchResults).atPositionOnView(index,R.id.locationName))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.searchResults).atPositionOnView(index,R.id.locationCountry))
                .check(matches(withText(result.country)))
        }
    }

    @Test
    fun highlightQueryInSearchResultsWhenResultsAvailable() {
        // TODO("Not yet implemented")
    }

    @Test
    fun navUpWhenUserCloseSearchField() {
        // Test case fixture
        mockkStatic(Fragment::findNavController)

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            every { fragment.findNavController().navigateUp() } returns true
        }
        every { viewModel.search(any()) } returns Unit

        // Given

        // And
        onView(instanceOf(SearchView::class.java))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // When user collapse search view
        pressBack()

        // Then
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            verify { fragment.findNavController().navigateUp() }
        }
    }

    @Test
    fun openWeatherDataScreenWhenResultSelected() {
        // Test case fixture
        val destId = 10
        val bundleSlot = slot<Bundle>()
        mockkStatic(Fragment::findNavController)

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            fragment.appNav = appNav
            every { fragment.findNavController().navigate(any(),capture(bundleSlot)) } returns Unit
        }

        every { appNav.getWeatherDest() } returns destId

        // Given

        // When
        val searchResults = createSearchResults()
        results.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // And
        onView(withId(R.id.searchResults))
            .perform(actionOnItemAtPosition<SavedLocationsAdapter.SavedLocationViewHolder>(0, click()))

        // Then
        verify { appNav.getWeatherDest() }

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationsFragment
            verify { fragment.findNavController().navigate(destId,any()) }
        }

        assertThat(bundleSlot.captured.get(LOCATION_LAT)).isEqualTo(searchResults.first().lat)
        assertThat(bundleSlot.captured.get(LOCATION_LON)).isEqualTo(searchResults.first().lon)
    }
}