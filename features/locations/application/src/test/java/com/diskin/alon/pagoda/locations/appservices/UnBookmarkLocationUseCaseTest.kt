package com.diskin.alon.pagoda.locations.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.UnBookmarkLocationRequest
import com.diskin.alon.pagoda.locations.appservices.usecase.UnBookmarkLocationUseCase
import com.diskin.alon.pagoda.locations.domain.Coordinates
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [UnBookmarkLocationUseCase] unit test class.
 */
class UnBookmarkLocationUseCaseTest {

    // Test subject
    private lateinit var useCase: UnBookmarkLocationUseCase

    // Collaborators
    private val repository: LocationRepository = mockk()
    private val mapper: Mapper<UnBookmarkLocationRequest, Coordinates> = mockk()

    @Before
    fun setUp() {
        useCase = UnBookmarkLocationUseCase(repository, mapper)
    }

    @Test
    fun removeSavedLocationWhenExecuted() {
        // Test case fixture
        val mappedId: Coordinates = mockk()
        val repoRes: Single<AppResult<Unit>> = mockk()

        every { repository.unBookmark(any()) } returns repoRes
        every { mapper.map(any()) } returns mappedId

        // Given

        // When
        val request = UnBookmarkLocationRequest(12.3,45.6)
        val useCaseRes = useCase.execute(request)

        // Then
        verify { mapper.map(request) }
        assertThat(useCaseRes).isEqualTo(repoRes)
    }
}