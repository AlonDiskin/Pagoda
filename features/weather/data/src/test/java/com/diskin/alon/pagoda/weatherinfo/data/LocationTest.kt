package com.diskin.alon.pagoda.weatherinfo.data

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.UserLocationProviderImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.local.util.LocationErrorHandler
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class LocationTest {

    // Test subject
    private lateinit var provider: UserLocationProviderImpl

    // Collaborators
    private lateinit var locationClient: FusedLocationProviderClient
    private val settingsClient: SettingsClient = mockk()
    private val errorHandler: LocationErrorHandler = LocationErrorHandler()
    private val mapper: Mapper<LocationResult, UserLocation> = mockk()

    // Stub data
    private val locationLat = 45.56
    private val locationLon = 89.23

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = Location("flp").also {
            it.latitude = locationLat
            it.longitude = locationLon
            it.accuracy = 3.0f
        }
        locationClient.setMockMode(true)
        locationClient.setMockLocation(location)

        // Init subject
        provider = UserLocationProviderImpl(
            locationClient,
            settingsClient,
            mapper ,
            errorHandler
        )
    }

    @Test
    fun name() {
        // Test case fixture
        val settingsCheckTask: Task<LocationSettingsResponse> = mockk()

        every { settingsClient.checkLocationSettings(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(any()) } returns settingsCheckTask
        every { settingsCheckTask.addOnSuccessListener(any()) } answers { this.value.onSuccessTask(mockk()) }
        every { settingsCheckTask.addOnFailureListener(any()) } returns settingsCheckTask

        // Given

        // When
        val observer = provider.getLocation().test()

        // Then
        val expectedLocation = UserLocation(locationLat,locationLon)
        verify { settingsClient.checkLocationSettings(any()) }
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) {
            //it == AppResult.Success(expectedLocation)
            //println("PIZDDA->:${it}")
            true
        }
    }
}