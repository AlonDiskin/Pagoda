package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.FavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.FavoriteLocationUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [FavoriteLocationUseCase] unit test class.
 */
class FavoriteLocationUseCaseTest {

    // Test subject
    private lateinit var useCase: FavoriteLocationUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<FavoriteLocationRequest, Coordinates> = mockk()

    @Before
    fun setUp() {
        useCase = FavoriteLocationUseCase(repository, mapper)
    }

    @Test
    fun addSavedLocationWhenExecuted() {
        // Test case fixture
        val mappedId: Coordinates = mockk()
        val repoRes: Single<AppResult<Unit>> = mockk()

        every { repository.favorite(any()) } returns repoRes
        every { mapper.map(any()) } returns mappedId

        // Given

        // When
        val request = FavoriteLocationRequest(12.3,45.6)
        val useCaseRes = useCase.execute(request)

        // Then
        verify { mapper.map(request) }
        assertThat(useCaseRes).isEqualTo(repoRes)
    }
}