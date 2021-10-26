package com.diskin.alon.pagoda.weather.infrastructure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.migration.OptionalInject
import io.reactivex.Single

@OptionalInject
@HiltWorker
class WeatherAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertProvider: WeatherAlertProvider
) : RxWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "alert notification channel id"
        const val CHANNEL_NAME = "alert notification channel"
        const val CHANNEL_DESCRIPTION = "weather alert notification"
        const val NOTIFICATION_ID = 100
    }

    override fun createWork(): Single<Result> {
        return alertProvider.get()
            .map {
                if (it.alertMessage.isNotEmpty()) {
                    showStatusBarNotification(it.alertMessage)
                }

                Result.success()
            }
            .onErrorReturn { Result.failure() }
    }

    private fun showStatusBarNotification(alertMessage: String) {
        val builder = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_text,alertMessage))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        createNotificationChannel()
        with(NotificationManagerCompat.from(applicationContext)) { notify(NOTIFICATION_ID, builder.build()) }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}