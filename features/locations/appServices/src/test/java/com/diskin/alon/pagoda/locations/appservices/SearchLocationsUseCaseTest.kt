package com.diskin.alon.pagoda.locations.appservices

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.appservices.model.SearchLocationsRequest
import com.diskin.alon.pagoda.locations.appservices.usecase.SearchLocationsUseCase
import com.diskin.alon.pagoda.locations.domain.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [SearchLocationsUseCase] unit test class.
 */
class SearchLocationsUseCaseTest {

    // Test subject
    private lateinit var useCase: SearchLocationsUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val locationMapper: Mapper<PagingData<Location>, PagingData<LocationSearchResult>> = mockk()

    @Before
    fun setUp() {
        useCase = SearchLocationsUseCase(repository, locationMapper)
    }

    @Test
    fun performSearchWhenExecuted() {
        // Test case fixture
        val repoPaging: PagingData<Location> = mockk()
        val mappedPaging: PagingData<LocationSearchResult> = mockk()

        every { repository.search(any()) } returns Observable.just(repoPaging)
        every { locationMapper.map(any()) } returns mappedPaging

        // Given

        // When
        val request = SearchLocationsRequest("query")
        val observer = useCase.execute(request).test()

        // Then
        verify { repository.search(request.query) }
        verify { locationMapper.map(repoPaging) }
        observer.assertValue(mappedPaging)
    }
}