package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.AppNavigator
import com.diskin.alon.pagoda.home.presentation.AppNavGraphProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Singleton
    @Binds
    abstract fun bindAppNavigator(navigator: AppNavigator): AppNavGraphProvider
}