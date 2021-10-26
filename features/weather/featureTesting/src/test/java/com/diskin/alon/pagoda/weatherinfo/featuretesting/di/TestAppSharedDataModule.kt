package com.diskin.alon.pagoda.weatherinfo.featuretesting.di

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import io.reactivex.Observable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppSharedDataModule {

    @Singleton
    @Provides
    fun provideTempUnitPrepProvider(): AppDataProvider<Observable<TempUnit>> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideWindSpeedUnitProvider(): AppDataProvider<Observable<WindSpeedUnit>> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideTimeFormatProvider(): AppDataProvider<Observable<TimeFormat>> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideAlertEnabledProvider(): AppDataProvider<Observable<WeatherAlertEnabled>> {
        return mockk()
    }
}