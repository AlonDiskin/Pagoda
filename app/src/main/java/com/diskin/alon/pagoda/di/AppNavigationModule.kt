package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.AppNavGraphProviderImpl
import com.diskin.alon.pagoda.home.presentation.AppNavGraphProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppNavigationModule {

    @Singleton
    @Provides
    fun provideAppNavigator(): AppNavGraphProvider {
        return AppNavGraphProviderImpl()
    }
}