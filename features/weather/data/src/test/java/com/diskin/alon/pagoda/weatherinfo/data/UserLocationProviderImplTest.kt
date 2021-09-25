package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.UserLocationProviderImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.local.util.LocationErrorHandler
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * [UserLocationProviderImpl] unit test class.
 */
class UserLocationProviderImplTest {

    // Test subject
    private lateinit var provider: UserLocationProviderImpl

    // Collaborators
    private val locationClient: FusedLocationProviderClient = mockk()
    private val settingsClient: SettingsClient = mockk()
    private val errorHandler: LocationErrorHandler = mockk()
    private val mapper: Mapper<LocationResult, UserLocation> = mockk()

    @Before
    fun setUp() {
        provider = UserLocationProviderImpl(locationClient, settingsClient, mapper ,errorHandler)
    }

    @Test
    fun provideDeviceCurrentLocation_WhenQueried() {
        // Given
        val settingsCheckTask: Task<LocationSettingsResponse> = mockk()
        val onSuccessListenerSlot = slot<OnSuccessListener<LocationSettingsResponse>>()
        val locationUpdatesTask: Task<Void> = mockk()
        val locationRequestSlot = slot<LocationRequest>()
        val locationCallbackSlot = slot<LocationCallback>()
        val removedCallbackSlot = slot<LocationCallback>()
        val mappedLocation: UserLocation = mockk()

        every { settingsClient.checkLocationSettings(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(capture(onSuccessListenerSlot)) } returns settingsCheckTask
        every { settingsCheckTask.addOnFailureListener(any()) } returns settingsCheckTask
        every { locationClient.requestLocationUpdates(capture(locationRequestSlot),
            capture(locationCallbackSlot),any()) } returns locationUpdatesTask
        every { locationUpdatesTask.addOnFailureListener(any()) } returns locationUpdatesTask
        every { locationClient.removeLocationUpdates(capture(removedCallbackSlot)) } returns mockk()
        every { mapper.map(any()) } returns mappedLocation

        // When
        val observer = provider.getLocation().test()

        // Then
        verify { settingsClient.checkLocationSettings(any()) }

        // When
        onSuccessListenerSlot.captured.onSuccess(mockk())

        // Then
        verify { locationClient.requestLocationUpdates(any(),any(),any()) }
        assertThat(locationRequestSlot.captured.priority).isEqualTo(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

        // When
        val locationRes: LocationResult = mockk()
        locationCallbackSlot.captured.onLocationResult(locationRes)

        // Then
        verify { mapper.map(locationRes) }
        observer.assertValue(Result.Success(mappedLocation))
        assertThat(locationCallbackSlot.captured).isEqualTo(removedCallbackSlot.captured)
    }

    @Test
    fun handleError_WhenLocationSettingCheckFail() {
        // Given
        val settingsCheckTask: Task<LocationSettingsResponse> = mockk()
        val onFailureListenerSlot = slot<OnFailureListener>()
        val onSuccessListenerSlot = slot<OnSuccessListener<LocationSettingsResponse>>()
        val appError: AppError = mockk()

        every { settingsClient.checkLocationSettings(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(capture(onSuccessListenerSlot)) } returns settingsCheckTask
        every { settingsCheckTask.addOnFailureListener(capture(onFailureListenerSlot)) } returns settingsCheckTask
        every { errorHandler.handle(any()) } returns appError

        // When
        val observer = provider.getLocation().test()

        // And
        val settingsError = Exception()
        onFailureListenerSlot.captured.onFailure(settingsError)

        // Then
        verify { errorHandler.handle(settingsError) }
        observer.assertValue(Result.Error(appError))
    }

    @Test
    fun handleError_WhenLocationPermissionNotGranted() {
        // Given
        val settingsCheckTask: Task<LocationSettingsResponse> = mockk()
        val onSuccessListenerSlot = slot<OnSuccessListener<LocationSettingsResponse>>()
        val locationUpdatesTask: Task<Void> = mockk()
        val onFailureListenerSlot = slot<OnFailureListener>()
        val appError: AppError = mockk()

        every { settingsClient.checkLocationSettings(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(capture(onSuccessListenerSlot)) } returns settingsCheckTask
        every { settingsCheckTask.addOnFailureListener(any()) } returns settingsCheckTask
        every { locationClient.requestLocationUpdates(any(), any(),any()) } returns locationUpdatesTask
        every { locationUpdatesTask.addOnFailureListener(capture(onFailureListenerSlot)) } returns locationUpdatesTask
        every { errorHandler.handle(any()) } returns appError

        // When
        val observer = provider.getLocation().test()

        // Then
        verify { settingsClient.checkLocationSettings(any()) }

        // When
        onSuccessListenerSlot.captured.onSuccess(mockk())

        // And
        val permissionError = Exception()
        onFailureListenerSlot.captured.onFailure(permissionError)

        // Then
        verify { errorHandler.handle(permissionError) }
        observer.assertValue(Result.Error(appError))
    }
}