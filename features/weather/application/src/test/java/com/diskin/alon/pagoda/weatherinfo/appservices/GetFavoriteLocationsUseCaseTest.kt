package com.diskin.alon.pagoda.weatherinfo.appservices

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetFavoriteLocationsUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [GetFavoriteLocationsUseCase] unit test class.
 */
class GetFavoriteLocationsUseCaseTest {

    // Test subject
    private lateinit var useCas: GetFavoriteLocationsUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<PagingData<Location>, PagingData<LocationDto>> = mockk()

    @Before
    fun setUp() {
        useCas = GetFavoriteLocationsUseCase(repository, mapper)
    }

    @Test
    fun fetchSavedLocationsWhenExecuted() {
        // Test case fixture
        val repoPaging: PagingData<Location> = mockk()
        val mappedPaging: PagingData<LocationDto> = mockk()

        every { repository.getFavorite() } returns Observable.just(repoPaging)
        every { mapper.map(any()) } returns mappedPaging

        // Given

        // When
        val observer = useCas.execute(Unit).test()

        // Then
        verify { repository.getFavorite() }
        verify { mapper.map(repoPaging) }
        observer.assertValue(mappedPaging)
    }
}