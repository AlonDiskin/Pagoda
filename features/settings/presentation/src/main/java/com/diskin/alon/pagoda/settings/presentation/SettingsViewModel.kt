package com.diskin.alon.pagoda.settings.presentation

import androidx.lifecycle.ViewModel
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventPublisher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val eventPublisher: WeatherUnitsEventPublisher
) : ViewModel() {

    fun updateWeatherUnit(unitSystemPref: UnitSystemEvent) {
        eventPublisher.post(unitSystemPref)
    }
}