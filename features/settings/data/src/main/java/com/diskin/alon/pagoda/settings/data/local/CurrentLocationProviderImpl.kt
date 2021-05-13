package com.diskin.alon.pagoda.settings.data.local

import android.annotation.SuppressLint
import android.os.HandlerThread
import android.os.Looper
import com.diskin.alon.pagoda.settings.data.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Single
import javax.inject.Inject

class CurrentLocationProviderImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient
) : CurrentLocationProvider {

    @SuppressLint("MissingPermission")
    override fun get(): Single<UserLocation> {
        return  Single.create { emitter ->
            val locationRequest = LocationRequest.create()
                .setWaitForAccurateLocation(true)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    emitter.onSuccess(
                        UserLocation(
                            p0.lastLocation.latitude,
                            p0.lastLocation.longitude
                        )
                    )
                    locationClient.removeLocationUpdates(this)
                }
            }

            val handler = HandlerThread("backgroundThread")
            handler.start()

            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                handler.looper)
                .addOnFailureListener { emitter.onError(it) }
        }
    }
}