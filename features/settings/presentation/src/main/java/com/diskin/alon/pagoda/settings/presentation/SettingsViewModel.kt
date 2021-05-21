package com.diskin.alon.pagoda.settings.presentation

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @SettingsModel private val model: Model
) : RxViewModel() {

    private val enableAlert = BehaviorSubject.create<Boolean>()
    val error = SingleLiveEvent<AppError>()

    init { addSubscription(createAlertSchedulingSubscription()) }

    private fun createAlertSchedulingSubscription(): Disposable {
        return enableAlert
            .switchMapSingle { model.execute(ScheduleAlertModelRequest(it)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleSchedulingResult, ::handleSchedulingSubscriptionError)
    }

    private fun handleSchedulingResult(result: AppResult<Unit>) {
        when(result) {
            is AppResult.Error -> error.value = result.error
        }
    }

    private fun handleSchedulingSubscriptionError(e: Throwable) {
        error.value = AppError(ErrorType.UNKNOWN_ERR)
        e.printStackTrace()
    }

    fun changeWeatherUnits(unit: WeatherUnit) {
        model.execute(UpdateWeatherUnitModelRequest(unit))
    }

    fun enableWeatherAlertNotification(enable: Boolean) {
        enableAlert.onNext(enable)
    }
}