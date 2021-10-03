package com.diskin.alon.pagoda.weatherinfo.appservices

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.SearchLocationsRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.SearchLocationsUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.Location
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
    private val locationMapper: Mapper<PagingData<Location>, PagingData<LocationDto>> = mockk()

    @Before
    fun setUp() {
        useCase = SearchLocationsUseCase(repository, locationMapper)
    }

    @Test
    fun performSearchWhenExecuted() {
        // Test case fixture
        val repoPaging: PagingData<Location> = mockk()
        val mappedPaging: PagingData<LocationDto> = mockk()

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