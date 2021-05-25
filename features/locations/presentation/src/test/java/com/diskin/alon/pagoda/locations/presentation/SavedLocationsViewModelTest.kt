package com.diskin.alon.pagoda.locations.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.locations.presentation.model.DeleteSavedLocationModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.SavedLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.viewmodel.SavedLocationsViewModel
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
 * [SavedLocationsViewModel] unit test class.
 */
class SavedLocationsViewModelTest {

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
    private lateinit var viewModel: SavedLocationsViewModel

    // Collaborators
    private val model: Model = mockk()

    // Sub data
    private val modelLocationsSubject = BehaviorSubject.create<PagingData<UiLocation>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { model.execute(any<SavedLocationsModelRequest>()) } returns modelLocationsSubject

        // Init test subject
        viewModel = SavedLocationsViewModel(model)
    }

    @Test
    fun observeModelSavedLocationsWhenCreated() {
        // Given

        // Then
        verify { model.execute(SavedLocationsModelRequest) }

        // When
        val paging: PagingData<UiLocation> = PagingData.from(emptyList())
        modelLocationsSubject.onNext(paging)

        // Then
        assertThat(viewModel.locations.value).isNotNull()
    }

    @Test
    fun deleteModelLocationWhenLocationDeleted() {
        // Test case fixture
        every { model.execute(any<DeleteSavedLocationModelRequest>()) } returns Single.just(AppResult.Success(Unit))

        // Given

        // When
        val location = UiLocation(34.5,67.8,"","")
        viewModel.deleteSavedLocation(location)

        // Then
        verify { model.execute(DeleteSavedLocationModelRequest(location.lat,location.lon)) }
    }

    @Test
    fun updateViewErrorWhenDeleteLocationFail() {
        // Test case fixture
        val error: AppError = mockk()
        every { model.execute(any<DeleteSavedLocationModelRequest>()) } returns Single.just(AppResult.Error(error))

        // Given

        // When
        val location = UiLocation(34.5,67.8,"","")
        viewModel.deleteSavedLocation(location)

        // Then
        verify { model.execute(DeleteSavedLocationModelRequest(location.lat,location.lon)) }

        // And
        assertThat(viewModel.error.value).isEqualTo(error)
    }
}