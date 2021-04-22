package com.diskin.alon.pagoda.locations.featuretesting.di

import com.diskin.alon.pagoda.locations.featuretesting.R
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestNavigationModule {

    @Singleton
    @Provides
    fun provideNavigationProvider(): AppLocationsNavProvider {
        return object : AppLocationsNavProvider {
            override fun getWeatherDest(): Int {
                return R.id.testWeatherInfoFragment
            }
        }
    }
}