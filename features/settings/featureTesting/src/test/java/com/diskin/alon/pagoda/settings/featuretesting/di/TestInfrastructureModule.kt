package com.diskin.alon.pagoda.settings.featuretesting.di

import com.diskin.alon.pagoda.settings.appservices.interfaces.WeatherAlertNotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestInfrastructureModule {

    @Singleton
    @Provides
    fun bindAlertScheduler(): WeatherAlertNotificationScheduler {
        return mockk()
    }
}