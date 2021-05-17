package com.diskin.alon.pagoda.locations.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.SearchLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SearchLocationsViewModel
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
 * [SearchLocationsViewModel] unit test class.
 */
class SearchLocationsViewModelTest {

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
    private lateinit var viewModel: SearchLocationsViewModel

    // Collaborators
    private val model: Model = mockk()
    private val savedState: SavedStateHandle = SavedStateHandle()

    // Stub data
    private val searchResultsSubject = BehaviorSubject.create<PagingData<UiLocation>>()
    private val modelRequestSlot = slot<ModelRequest<*, *>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(capture(modelRequestSlot)) } returns searchResultsSubject

        // Init subject
        viewModel = SearchLocationsViewModel(model, savedState)
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
        savedState.set(SearchLocationsViewModel.KEY_QUERY,savedQuery)
        viewModel = SearchLocationsViewModel(model,savedState)

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
        verify { model.execute(any<SearchLocationsModelRequest>()) }
        assertThat((modelRequestSlot.captured as SearchLocationsModelRequest).query).isEqualTo(query)

        // And
        assertThat(viewModel.results.value).isNull()
        val paging: PagingData<UiLocation> = PagingData.from(emptyList())
        searchResultsSubject.onNext(paging)

        // Then
        assertThat(viewModel.results.value).isNotNull()
    }

    @Test
    fun performModelLocationsSearchWhenCreatedWithSavedState() {
        // Given
        val savedQuery = "query"
        savedState.set(SearchLocationsViewModel.KEY_QUERY,savedQuery)
        viewModel = SearchLocationsViewModel(model,savedState)

        // Then
        verify { model.execute(any<SearchLocationsModelRequest>()) }
        assertThat((modelRequestSlot.captured as SearchLocationsModelRequest).query).isEqualTo(savedQuery)
    }
}