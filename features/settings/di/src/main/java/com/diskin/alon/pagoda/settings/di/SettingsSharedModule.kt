package com.diskin.alon.pagoda.settings.di

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.shared.TempUnitProvider
import com.diskin.alon.pagoda.settings.presentation.shared.TimeFormatProvider
import com.diskin.alon.pagoda.settings.presentation.shared.WeatherAlertProvider
import com.diskin.alon.pagoda.settings.presentation.shared.WindSpeedUnitProvider
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Observable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsSharedModule {

    @Singleton
    @Binds
    abstract fun bindTempUnitProvider(provider: TempUnitProvider): AppDataProvider<Observable<TempUnit>>

    @Singleton
    @Binds
    abstract fun bindWindSpeedUnitProvider(provider: WindSpeedUnitProvider): AppDataProvider<Observable<WindSpeedUnit>>


    @Singleton
    @Binds
    abstract fun bindTimeFormatProvider(provider: TimeFormatProvider): AppDataProvider<Observable<TimeFormat>>

    @Singleton
    @Binds
    abstract fun bindWeatherAlertProvider(provider: WeatherAlertProvider): AppDataProvider<Observable<WeatherAlertEnabled>>

}