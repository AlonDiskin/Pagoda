package com.diskin.alon.pagoda.settings.featuretesting.di

import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestDataModule {

    @Singleton
    @Provides
    fun bindWeatherAlertProvider(): WeatherAlertProvider {
        return mockk()
    }
}