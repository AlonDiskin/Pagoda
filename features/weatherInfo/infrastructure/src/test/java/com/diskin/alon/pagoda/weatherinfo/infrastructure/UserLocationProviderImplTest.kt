package com.diskin.alon.pagoda.weatherinfo.infrastructure

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
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
        provider = UserLocationProviderImpl(locationClient, settingsClient, errorHandler, mapper)
    }

    @Test
    fun provideDeviceCurrentLocationWhenQueried() {
        // Test case fixture
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

        // Given an initialized provider

        // When provider is queried for current device location
        val observer = provider.getCurrentLocation().test()

        // Then provider should check location settings on device
        verify { settingsClient.checkLocationSettings(any()) }

        // When settings check is successful
        onSuccessListenerSlot.captured.onSuccess(mockk())

        // Then provider should register location updates callback
        verify { locationClient.requestLocationUpdates(any(),any(),any()) }
        assertThat(locationRequestSlot.captured.priority).isEqualTo(LocationRequest.PRIORITY_HIGH_ACCURACY)
        assertThat(locationRequestSlot.captured.isWaitForAccurateLocation).isTrue()

        // When location updated is issued to callback listener
        val locationRes: LocationResult = mockk()
        locationCallbackSlot.captured.onLocationResult(locationRes)

        // Then provider should propagate first mapped update result and unregister callback listener
        verify { mapper.map(locationRes) }
        observer.assertValue(AppResult.Success(mappedLocation))
        assertThat(locationCallbackSlot.captured).isEqualTo(removedCallbackSlot.captured)
    }

    @Test
    fun handleErrorWhenLocationSettingCheckFail() {
        // Test case fixture
        val settingsCheckTask: Task<LocationSettingsResponse> = mockk()
        val onFailureListenerSlot = slot<OnFailureListener>()
        val onSuccessListenerSlot = slot<OnSuccessListener<LocationSettingsResponse>>()
        val appError: AppError = mockk()

        every { settingsClient.checkLocationSettings(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(capture(onSuccessListenerSlot)) } returns settingsCheckTask
        every { settingsCheckTask.addOnFailureListener(capture(onFailureListenerSlot)) } returns settingsCheckTask
        every { errorHandler.handle(any()) } returns appError

        // Given an initialized provider

        // When provider is queried for current device location
        val observer = provider.getCurrentLocation().test()

        // And settings check results in failure
        val settingsError = Exception()
        onFailureListenerSlot.captured.onFailure(settingsError)

        // Then provider should propagate handled error result
        verify { errorHandler.handle(settingsError) }
        observer.assertValue(AppResult.Error(appError))
    }

    @Test
    fun handleErrorWhenLocationPermissionNotGranted() {
        // Test case fixture
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

        // Given an initialized provider

        // When provider is queried for current device location
        val observer = provider.getCurrentLocation().test()

        // Then provider should check location settings on device
        verify { settingsClient.checkLocationSettings(any()) }

        // When settings check is successful
        onSuccessListenerSlot.captured.onSuccess(mockk())

        // And location update request fail dou to missing permission
        val permissionError = Exception()
        onFailureListenerSlot.captured.onFailure(permissionError)

        // Then provider should propagate handled error result
        verify { errorHandler.handle(permissionError) }
        observer.assertValue(AppResult.Error(appError))
    }
}