package com.diskin.alon.pagoda.locations.appservices

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.appservices.usecase.BrowseSavedLocationsUseCase
import com.diskin.alon.pagoda.locations.domain.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [BrowseSavedLocationsUseCase] unit test class.
 */
class BrowseSavedLocationsUseCaseTest {

    // Test subject
    private lateinit var useCas: BrowseSavedLocationsUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<PagingData<Location>, PagingData<LocationDto>> = mockk()

    @Before
    fun setUp() {
        useCas = BrowseSavedLocationsUseCase(repository, mapper)
    }

    @Test
    fun fetchSavedLocationsWhenExecuted() {
        // Test case fixture
        val repoPaging: PagingData<Location> = mockk()
        val mappedPaging: PagingData<LocationDto> = mockk()

        every { repository.getSaved() } returns Observable.just(repoPaging)
        every { mapper.map(any()) } returns mappedPaging

        // Given

        // When
        val observer = useCas.execute(Unit).test()

        // Then
        verify { repository.getSaved() }
        verify { mapper.map(repoPaging) }
        observer.assertValue(mappedPaging)
    }
}