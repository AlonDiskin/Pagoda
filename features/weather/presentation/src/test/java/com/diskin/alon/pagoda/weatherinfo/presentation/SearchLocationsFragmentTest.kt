package com.diskin.alon.pagoda.weatherinfo.presentation

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
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
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withSearchViewHint
import com.diskin.alon.pagoda.common.uitesting.withSearchViewQuery
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.BookmarkedLocationsAdapter.BookmarkedLocationViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.SearchLocationsFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocationSearchResult
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.SearchLocationsViewModel
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
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val results = MutableLiveData<PagingData<UiLocationSearchResult>>()
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
        scenario = launchFragmentInHiltContainer<SearchLocationsFragment>()

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as SearchLocationsFragment

            navController.setGraph(R.navigation.weather_graph)
            navController.setCurrentDestination(R.id.searchLocationsFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
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
            val fragment = it.supportFragmentManager.fragments.first() as SearchLocationsFragment
            fragment.handleSearchResultsLoadStates(
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
    fun hideProgressBarWhenResultsAvailable() {
        // Given a resumed fragment

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as SearchLocationsFragment
            fragment.handleSearchResultsLoadStates(
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
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageButton(any(),any()) } returns Unit
        scenario.onActivity { it.findViewById<RecyclerView>(R.id.search_location_results).itemAnimator = null }

        // Given a resumed fragment

        // When
        val searchResults = createSearchResults()
        results.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.search_location_results)
            val adapter = recyclerView.adapter!!

            assertThat(adapter.itemCount).isEqualTo(searchResults.size)
        }

        searchResults.forEachIndexed { index, result ->
            onView(withId(R.id.search_location_results))
                .perform(scrollToPosition<LocationSearchResultViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.search_location_results).atPositionOnView(index,R.id.location_name))
                .check(matches(withText(result.name)))

            onView(withRecyclerView(R.id.search_location_results).atPositionOnView(index,R.id.location_country))
                .check(matches(withText(result.country)))

            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.search_location_results)
                val imageButton = recyclerView.findViewHolderForAdapterPosition(index)?.itemView
                    ?.findViewById<ImageButton>(R.id.add_bookmark_button)!!

                when(result.bookmarked) {
                    true -> verify { ImageLoader.loadIconResIntoImageButton(imageButton,R.drawable.ic_baseline_done_24) }
                    false -> verify { ImageLoader.loadIconResIntoImageButton(imageButton,R.drawable.ic_baseline_add_24) }
                }
            }
        }
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
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val searchResults = createSearchResults()
        results.value = PagingData.from(searchResults)
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
        onView(withId(R.id.search_location_results))
            .perform(actionOnItemAtPosition<BookmarkedLocationViewHolder>(0, click()))

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.weatherFragment)
        assertThat(bundle?.getDouble(context.getString(R.string.arg_lat_key))).isEqualTo(searchResults.first().lat)
        assertThat(bundle?.getDouble(context.getString(R.string.arg_lon_key))).isEqualTo(searchResults.first().lon)
    }

    @Test
    fun addBookmarkLocationResultWhenBookmarkedByUser() {
        // Test case fixture
        scenario.onActivity { it.findViewById<RecyclerView>(R.id.search_location_results).itemAnimator = null }
        every { viewModel.addToBookmarked(any()) } returns Unit

        // Given

        // When
        val result = UiLocationSearchResult(12.5,67.8,"","",false)
        results.value = PagingData.from(listOf(result))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.add_bookmark_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.addToBookmarked(result) }
    }

    @Test
    fun disableAddingResultToBookmarkedWhenAlreadyAdded() {
        // Test case fixture
        scenario.onActivity { it.findViewById<RecyclerView>(R.id.search_location_results).itemAnimator = null }
        every { viewModel.addToBookmarked(any()) } returns Unit

        // Given

        // When
        val result = UiLocationSearchResult(12.5,67.8,"","",true)
        results.value = PagingData.from(listOf(result))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.add_bookmark_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 0) { viewModel.addToBookmarked(result) }
    }
}