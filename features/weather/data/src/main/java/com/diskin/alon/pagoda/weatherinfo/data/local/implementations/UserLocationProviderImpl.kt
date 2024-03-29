package com.diskin.alon.pagoda.weatherinfo.data.local.implementations

import android.annotation.SuppressLint
import android.os.Looper
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.appservices.results.toSingleResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.local.util.LocationErrorHandler
import com.google.android.gms.location.*
import io.reactivex.Single
import javax.inject.Inject

class UserLocationProviderImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient,
    private val locationMapper: Mapper<LocationResult, UserLocation>,
    private val errorHandler: LocationErrorHandler
) : UserLocationProvider {

    @SuppressLint("MissingPermission")
    override fun getLocation(): Single<Result<UserLocation>> {
        return Single.create<UserLocation> { emitter ->
            // Set the location request and settings request for needed location functionality
            // from device
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()
            // Define the location updates callback to only receive one location update and then
            // terminate updates, and propagate location result downstream
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    emitter.onSuccess(locationMapper.map(p0))
                    locationClient.removeLocationUpdates(this)
                }
            }

            // Check if all required location settings are satisfied
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    // Location settings in device are satisfied for location request, ask for
                    // locations update from client
                    locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper())
                        // handle location permission error
                        .addOnFailureListener { emitter.onError(it) }
                }
                // handle device location setting error
                .addOnFailureListener{ emitter.onError(it) }
        }
            .toSingleResult(errorHandler::handle)
    }
}