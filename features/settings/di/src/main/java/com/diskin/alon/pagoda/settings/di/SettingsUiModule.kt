package com.diskin.alon.pagoda.settings.di

import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.usecase.ChangeWeatherUnitUseCase
import com.diskin.alon.pagoda.settings.appservices.usecase.ScheduleWeatherAlertNotificationUseCase
import com.diskin.alon.pagoda.settings.presentation.ScheduleAlertModelRequest
import com.diskin.alon.pagoda.settings.presentation.SettingsModel
import com.diskin.alon.pagoda.settings.presentation.UpdateWeatherUnitModelRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SettingsUiModule {

    @SettingsModel
    @Provides
    fun provideModelDispatcherMap(
        updateWeatherUnitUseCase: ChangeWeatherUnitUseCase,
        enableWeatherAlertUseCase: ScheduleWeatherAlertNotificationUseCase
    ): Model {
        val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

        map[UpdateWeatherUnitModelRequest::class.java] = Pair(updateWeatherUnitUseCase,null)
        map[ScheduleAlertModelRequest::class.java] = Pair(enableWeatherAlertUseCase,null)

        return ModelDispatcher(map)
    }
}