package com.diskin.alon.pagoda.settings.presentation

import androidx.lifecycle.ViewModel
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @SettingsModel private val model: Model
) : ViewModel() {

    fun updateWeatherUnits(unit: WeatherUnit) {
        model.execute(UpdateWeatherUnitModelRequest(unit))
    }
}