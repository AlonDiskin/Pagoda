package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.shared.AppDataPublisher
import com.diskin.alon.pagoda.weather.infrastructure.AlertScheduleErrorDataHandler
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Observable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherSharedModule {

    @Singleton
    @Binds
    abstract fun bindAlertScheduleErrorProvider(provider: AlertScheduleErrorDataHandler): AppDataProvider<Observable<AlertSchedulingError>>

    @Singleton
    @Binds
    abstract fun bindAlertScheduleErrorPublisher(publisher: AlertScheduleErrorDataHandler): AppDataPublisher<AlertSchedulingError>
}