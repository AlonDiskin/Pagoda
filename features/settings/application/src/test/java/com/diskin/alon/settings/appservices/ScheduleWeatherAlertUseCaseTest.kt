package com.diskin.alon.settings.appservices

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.interfaces.WeatherAlertNotificationScheduler
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import com.diskin.alon.pagoda.settings.appservices.model.ScheduleAlertRequest
import com.diskin.alon.pagoda.settings.appservices.usecase.ScheduleWeatherAlertNotificationUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * [ScheduleWeatherAlertNotificationUseCase] unit test class.
 */
class ScheduleWeatherAlertUseCaseTest {

    // Test subject
    private lateinit var useCase: ScheduleWeatherAlertNotificationUseCase

    // Collaborators
    private val scheduler: WeatherAlertNotificationScheduler = mockk()
    private val mapper: Mapper<ScheduleAlertRequest, AlertInfo> = mockk()

    @Before
    fun setUp() {
        useCase = ScheduleWeatherAlertNotificationUseCase(scheduler, mapper)
    }

    @Test
    fun scheduleAlertWhenExecuted() {
        // Test ase fixture
        val mappedRequest: AlertInfo = mockk()

        every { scheduler.schedule(any()) } returns mockk()
        every { mapper.map(any()) } returns mappedRequest

        // Given

        // When
        val request = ScheduleAlertRequest(true)
        useCase.execute(request)

        // Then
        verify { mapper.map(request) }
        verify { scheduler.schedule(mappedRequest) }
    }
}