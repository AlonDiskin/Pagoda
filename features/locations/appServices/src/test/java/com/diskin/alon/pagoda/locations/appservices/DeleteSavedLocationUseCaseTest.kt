package com.diskin.alon.pagoda.locations.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.DeleteSavedLocationRequest
import com.diskin.alon.pagoda.locations.appservices.usecase.DeleteSavedLocationUseCase
import com.diskin.alon.pagoda.locations.domain.Coordinates
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [DeleteSavedLocationUseCase] unit test class.
 */
class DeleteSavedLocationUseCaseTest {

    // Test subject
    private lateinit var useCase: DeleteSavedLocationUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<DeleteSavedLocationRequest, Coordinates> = mockk()

    @Before
    fun setUp() {
        useCase = DeleteSavedLocationUseCase(repository, mapper)
    }

    @Test
    fun removeSavedLocationWhenExecuted() {
        // Test case fixture
        val mappedId: Coordinates = mockk()
        val repoRes: Single<AppResult<Unit>> = mockk()

        every { repository.unSave(any()) } returns repoRes
        every { mapper.map(any()) } returns mappedId

        // Given

        // When
        val request = DeleteSavedLocationRequest(12.3,45.6)
        val useCaseRes = useCase.execute(request)

        // Then
        verify { mapper.map(request) }
        assertThat(useCaseRes).isEqualTo(repoRes)
    }
}