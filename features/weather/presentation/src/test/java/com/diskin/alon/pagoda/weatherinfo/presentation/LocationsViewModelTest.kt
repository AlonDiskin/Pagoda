package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.weatherinfo.presentation.model.FavoriteLocationsModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.SearchLocationsModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.LocationsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [LocationsViewModel] unit test class.
 */
class LocationsViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: LocationsViewModel

    // Collaborators
    private val model: Model = mockk()
    private val savedState: SavedStateHandle = SavedStateHandle()

    // Stub data
    private val locationsSubject = BehaviorSubject.create<PagingData<UiLocation>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<SearchLocationsModelRequest>()) } returns locationsSubject
        every { model.execute(any<FavoriteLocationsModelRequest>()) } returns locationsSubject

        // Init subject
        viewModel = LocationsViewModel(model, savedState)
    }

    @Test
    fun requestModelFavoriteLocations_WhenCreatedWithoutPrevState() {
        // Given

        // Then
        verify { model.execute(FavoriteLocationsModelRequest) }
    }

    @Test
    fun searchModelLocations_WhenCreatedWithPrevState() {
        // Given
        val savedQuery = "query"
        savedState[LocationsViewModel.KEY_QUERY] = savedQuery

        // When
        viewModel = LocationsViewModel(model, savedState)

        // Then
        verify { model.execute(SearchLocationsModelRequest(savedQuery)) }
    }

    @Test
    fun updateViewLocations_WhenModelUpdates() {
        // Given
        val locations = PagingData.from(createFavoriteLocations())

        // When
        locationsSubject.onNext(locations)

        // Then
        // TODO find a way to verify paging data equality
    }

    @Test
    fun saveQueryState_WhenSearched() {
        // Given
        val query = "query"

        // When
        viewModel.search(query)

        // Then
        assertThat(savedState.get<String>(LocationsViewModel.KEY_QUERY)).isEqualTo(query)
    }

    @Test
    fun searchModelLocations_WhenSearched() {
        // Given
        val query = "query"

        // When
        viewModel.search(query)

        // Then
        verify { model.execute(SearchLocationsModelRequest(query)) }
    }

    @Test
    fun requestModelFavoriteLocations_WhenAskedToLoadFavorites() {
        // Given

        // When
        viewModel.loadFavorites()

        // Then
        verify(exactly = 2) { model.execute(FavoriteLocationsModelRequest) }
    }

    @Test
    fun saveSearchViewExpandedState_WhenSet() {
        // Given
        val isExpanded = true

        // When
        viewModel.searchViewExpanded = isExpanded

        // Then
        assertThat(savedState.get<Boolean>(LocationsViewModel.KEY_SEARCH_EXPANDED)).isEqualTo(isExpanded)
    }

    @Test
    fun restoreSearchViewExpandedState_WhenCreatedWithPrevState() {
        // Given
        val prevExpandedState = true
        savedState[LocationsViewModel.KEY_SEARCH_EXPANDED] = prevExpandedState

        // When
        viewModel = LocationsViewModel(model, savedState)

        // Then
        assertThat(viewModel.searchViewExpanded).isEqualTo(prevExpandedState)
    }

    @Test
    fun setSearchViewExpandedAsFalse_WhenCreatedWithoutPrevState() {
        // Given

        // Then
        assertThat(viewModel.searchViewExpanded).isFalse()
    }

    @Test
    fun restoreSearchQueryState_WhenCreatedWithPrevState() {
        // Given
        val prevState = "query"
        savedState[LocationsViewModel.KEY_QUERY] = prevState

        // When
        viewModel = LocationsViewModel(model, savedState)

        // Then
        assertThat(viewModel.query).isEqualTo(prevState)
    }

    @Test
    fun setSearchQueryAsEmpty_WhenCreatedWithoutPrevState() {
        // Given

        // Then
        assertThat(viewModel.query).isEmpty()
    }
}