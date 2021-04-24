package com.diskin.alon.pagoda.locations.presentation

import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.paging.*
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
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withSearchViewHint
import com.diskin.alon.pagoda.common.uitesting.withSearchViewQuery
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.locations.presentation.controller.SearchLocationFragment
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SearchLocationViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.CopyOnWriteArrayList

/**
 * [SearchLocationFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class SearchLocationFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: SearchLocationViewModel = mockk()
    private val appNav: AppLocationsNavProvider = mockk()

    // Stub data
    private val results = MutableLiveData<PagingData<LocationSearchResult>>()
    private val query = "query"

    // Test nav controller
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.query } returns query
        every { viewModel.results } returns results

        // Setup test nav controller
        navController.setGraph(R.navigation.test_locations_nav_graph)
        navController.setCurrentDestination(R.id.searchLocationFragment)

        // Launch fragment under test

        // Fragment scenario has no action bar that needed for test so we use hilt
        // test activity(uses activity scenario)
        scenario = launchFragmentInHiltContainer<SearchLocationFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
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

            onView(withRecyclerView(R.id.searchResults).atPositionOnView(index,R.id.locationState))
                .check(matches(withText(", ${result.state}")))
        }
    }

    @Test
    fun highlightQueryInSearchResultsWhenResultsAvailable() {
        // TODO("Not yet implemented")
    }

    @Test
    fun navUpWhenUserCloseSearchField() {
        // Test case fixture
        every { viewModel.search(any()) } returns Unit

        // Given

        // And
        onView(instanceOf(SearchView::class.java))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // When user collapse search view
        pressBack()

        // Then
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.homeFragment)
    }

    @Test
    fun openWeatherDataScreenWhenResultSelected() {
        // Test case fixture
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as SearchLocationFragment
            fragment.appNav = appNav
        }

        every { appNav.getWeatherDest() } returns R.id.testWeatherInfoFragment

        // Given

        // When
        val searchResults = createSearchResults()
        results.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // And
        onView(withId(R.id.searchResults))
            .perform(actionOnItemAtPosition<LocationSearchResultViewHolder>(0,click()))

        // Then
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.testWeatherInfoFragment)
        assertThat(navController.currentBackStackEntry?.arguments?.get(LOCATION_LAT))
            .isEqualTo(searchResults.first().lat)
        assertThat(navController.currentBackStackEntry?.arguments?.get(LOCATION_LON))
            .isEqualTo(searchResults.first().lon)
    }

    private fun getSearchResultsAdapterLoadStatesListener(adapter: LocationSearchResultsAdapter): (CombinedLoadStates) -> Unit {
        val field = PagingDataAdapter::class.java.getDeclaredField("differ")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val differ = field.get(adapter) as AsyncPagingDataDiffer<LocationSearchResult>

        val field2 = AsyncPagingDataDiffer::class.java.getDeclaredField("differBase")
        field2.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val differBase = field2.get(differ) as PagingDataDiffer<LocationSearchResult>

        val field3 = PagingDataDiffer::class.java.getDeclaredField("loadStateListeners")
        field3.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val listeners = field3.get(differBase) as CopyOnWriteArrayList<(CombinedLoadStates) -> Unit>

        return listeners[1]
    }
}