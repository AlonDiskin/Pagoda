package com.diskin.alon.pagoda.weather.infrastructure

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.*
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherAlertNotificationScheduler
import com.diskin.alon.pagoda.weatherinfo.appservices.model.AlertInfoDto
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherAlertNotificationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val app: Application
) : WeatherAlertNotificationScheduler {

    companion object {
        const val WEATHER_ALERT_WORK_NAME = "weather alert work"
        const val WORK_INTERVAL_MINUTES = 30L
    }

    override fun schedule(info: AlertInfoDto): Single<AppResult<Unit>> {
        return when(info.enabled) {
            true -> enqueueWeatherAlertWork()
            else -> cancelWeatherAlertWork()
        }.subscribeOn(Schedulers.computation())
    }

    private fun enqueueWeatherAlertWork(): Single<AppResult<Unit>>{
        return Single.create {
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
                    WORK_INTERVAL_MINUTES,
                    TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    WEATHER_ALERT_WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest)

                it.onSuccess(AppResult.Success(Unit))
            } else {
                it.onSuccess(AppResult.Error(AppError(ErrorType.LOCATION_BACKGROUND_PERMISSION)))
            }
        }
    }

    private fun cancelWeatherAlertWork(): Single<AppResult<Unit>> {
        return Single.create {
            workManager.cancelUniqueWork(WEATHER_ALERT_WORK_NAME)
            it.onSuccess(AppResult.Success(Unit))
        }
    }
}