package com.diskin.alon.pagoda.settings.infrastructure.implementation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.*
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType.LOCATION_BACKGROUND_PERMISSION
import com.diskin.alon.pagoda.settings.appservices.interfaces.WeatherAlertNotificationScheduler
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
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

    override fun schedule(info: AlertInfo): Single<AppResult<Unit>> {
        val scheduled = when(info.enable) {
            true -> enqueueWeatherAlertWork()
            else -> cancelWeatherAlertWork()
        }

        return scheduled.subscribeOn(Schedulers.newThread())
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
                it.onSuccess(AppResult.Error(AppError(LOCATION_BACKGROUND_PERMISSION)))
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