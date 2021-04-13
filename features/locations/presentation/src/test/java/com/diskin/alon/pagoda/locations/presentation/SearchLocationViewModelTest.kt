package com.diskin.alon.pagoda.locations.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.presentation.model.SearchModelRequest
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SearchLocationViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [SearchLocationViewModel] unit test class.
 */
class SearchLocationViewModelTest {

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
    private lateinit var viewModel: SearchLocationViewModel

    // Collaborators
    private val model: Model = mockk()
    private val savedState: SavedStateHandle = SavedStateHandle()

    // Stub data
    private val searchResultsSubject = BehaviorSubject.create<PagingData<LocationSearchResult>>()
    private val modelRequestSlot = slot<ModelRequest<*, *>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(capture(modelRequestSlot)) } returns searchResultsSubject

        // Init subject
        viewModel = SearchLocationViewModel(model, savedState)
    }

    @Test
    fun initEmptyQueryWhenCreatedWithoutSavedState() {
        // Given

        // Then
        assertThat(viewModel.query).isEmpty()
    }

    @Test
    fun initSavedQueryWhenCreatedWithSavedState() {
        // Given
        val savedQuery = "query"
        savedState.set(SearchLocationViewModel.KEY_QUERY,savedQuery)
        viewModel = SearchLocationViewModel(model,savedState)

        // Then
        assertThat(viewModel.query).isEqualTo(savedQuery)
    }

    @Test
    fun performModelLocationsSearchWhenSearched() {
        // Given

        // When
        val query = "query"
        viewModel.search(query)

        // Then
        verify { model.execute(any<SearchModelRequest>()) }
        assertThat((modelRequestSlot.captured as SearchModelRequest).query).isEqualTo(query)

        // And
        assertThat(viewModel.results.value).isNull()
        val paging: PagingData<LocationSearchResult> = PagingData.from(emptyList())
        searchResultsSubject.onNext(paging)

        // Then
        assertThat(viewModel.results.value).isNotNull()
    }

    @Test
    fun performModelLocationsSearchWhenCreatedWithSavedState() {
        // Given
        val savedQuery = "query"
        savedState.set(SearchLocationViewModel.KEY_QUERY,savedQuery)
        viewModel = SearchLocationViewModel(model,savedState)

        // Then
        verify { model.execute(any<SearchModelRequest>()) }
        assertThat((modelRequestSlot.captured as SearchModelRequest).query).isEqualTo(savedQuery)
    }
}