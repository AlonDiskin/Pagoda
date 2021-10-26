package com.diskin.alon.pagoda.weatherinfo.di

import android.app.Application
import androidx.work.WorkManager
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertNotificationSchedulerImpl
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherAlertNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherInfrastructureModule {

    companion object {
        @Singleton
        @Provides
        fun provideWorkManager(app: Application): WorkManager {
            return WorkManager.getInstance(app)
        }
    }

    @Singleton
    @Binds
    abstract fun bindAlertScheduler(scheduler: WeatherAlertNotificationSchedulerImpl): WeatherAlertNotificationScheduler
}