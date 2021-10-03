package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.weatherinfo.presentation.model.BookmarkedLocationsModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiBookmarkedLocation
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UnBookmarkLocationModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.BookmarkedLocationsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [BookmarkedLocationsViewModel] unit test class.
 */
class BookmarkedLocationsViewModelTest {

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
    private lateinit var viewModel: BookmarkedLocationsViewModel

    // Collaborators
    private val model: Model = mockk()

    // Sub data
    private val modelLocationsSubject = BehaviorSubject.create<PagingData<UiBookmarkedLocation>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<BookmarkedLocationsModelRequest>()) } returns modelLocationsSubject

        // Init test subject
        viewModel = BookmarkedLocationsViewModel(model)
    }

    @Test
    fun requestBookmarkedLocationsFromModel_WhenCreated() {
        // Given

        // Then
        verify { model.execute(BookmarkedLocationsModelRequest) }
    }

    @Test
    fun deleteModelLocation_WhenLocationDeleted() {
        // Test case fixture
        every { model.execute(any<UnBookmarkLocationModelRequest>()) } returns Single.just(AppResult.Success(Unit))

        // Given

        // When
        val location = UiBookmarkedLocation(34.5,67.8,"","")
        viewModel.deleteSavedLocation(location)

        // Then
        verify { model.execute(UnBookmarkLocationModelRequest(location.lat,location.lon)) }
    }

    @Test
    fun updateViewError_WhenDeleteLocationFail() {
        // Test case fixture
        val error: AppError = mockk()
        every { model.execute(any<UnBookmarkLocationModelRequest>()) } returns Single.just(AppResult.Error(error))

        // Given

        // When
        val location = UiBookmarkedLocation(34.5,67.8,"","")
        viewModel.deleteSavedLocation(location)

        // Then
        verify { model.execute(UnBookmarkLocationModelRequest(location.lat,location.lon)) }

        // And
        assertThat(viewModel.error.value).isEqualTo(error)
    }
}