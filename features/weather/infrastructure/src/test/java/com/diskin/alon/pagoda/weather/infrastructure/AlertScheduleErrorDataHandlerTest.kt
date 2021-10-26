package com.diskin.alon.pagoda.weather.infrastructure

import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

/**
 * [AlertScheduleErrorDataHandler] unit test class.
 */
class AlertScheduleErrorDataHandlerTest {

    // Test subject
    private lateinit var handler: AlertScheduleErrorDataHandler

    @Before
    fun setUp() {
        handler = AlertScheduleErrorDataHandler()
    }

    @Test
    fun passErrorToSubscribers_WhenErrorPublished() {
        // Given
        val error: AlertSchedulingError = mockk()
        val observer = handler.get().test()

        //When
        handler.publish(error)

        // Then
        observer.assertValue(error)
    }
}