package com.diskin.alon.pagoda.weatherinfo.featuretesting.di

import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
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
    fun provideUserLocationProvider(): UserLocationProvider {
        return mockk()
    }
}