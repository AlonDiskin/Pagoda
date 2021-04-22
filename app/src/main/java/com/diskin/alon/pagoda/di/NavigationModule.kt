package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.AppNavigator
import com.diskin.alon.pagoda.home.presentation.AppHomeNavProvider
import com.diskin.alon.pagoda.locations.presentation.controller.AppLocationsNavProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    @Singleton
    @Provides
    fun provideAppNavigator(): AppNavigator {
        return AppNavigator()
    }

    @Singleton
    @Provides
    fun provideAppHomeNavProvider(appNavigator: AppNavigator): AppHomeNavProvider {
        return appNavigator
    }

    @Singleton
    @Provides
    fun provideAppLocationsNavProvider(appNavigator: AppNavigator): AppLocationsNavProvider {
        return appNavigator
    }
}