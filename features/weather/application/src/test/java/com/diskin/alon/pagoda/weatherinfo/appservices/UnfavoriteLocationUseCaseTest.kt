package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnfavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.UnfavoriteLocationUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [UnfavoriteLocationUseCase] unit test class.
 */
class UnfavoriteLocationUseCaseTest {

    // Test subject
    private lateinit var useCase: UnfavoriteLocationUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<UnfavoriteLocationRequest, Coordinates> = mockk()

    @Before
    fun setUp() {
        useCase = UnfavoriteLocationUseCase(repository, mapper)
    }

    @Test
    fun removeSavedLocationWhenExecuted() {
        // Test case fixture
        val mappedId: Coordinates = mockk()
        val repoRes: Single<AppResult<Unit>> = mockk()

        every { repository.unfavorite(any()) } returns repoRes
        every { mapper.map(any()) } returns mappedId

        // Given

        // When
        val request = UnfavoriteLocationRequest(12.3,45.6)
        val useCaseRes = useCase.execute(request)

        // Then
        verify { mapper.map(request) }
        assertThat(useCaseRes).isEqualTo(repoRes)
    }
}