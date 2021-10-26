package com.diskin.alon.pagoda.settings.presentation.viewmodel

import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.controller.ThemeManager
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val alertError: AppDataProvider<Observable<AlertSchedulingError>>,
    private val themeManager: ThemeManager
) : RxViewModel() {

    val error = SingleLiveEvent<AppError>()

    init {
        addSubscription(createAlertSchedulingFailSubscription())
    }

    fun enableDarkMode(enable: Boolean) {
        themeManager.enableDarkMode(enable)
    }

    private fun createAlertSchedulingFailSubscription(): Disposable {
        return alertError.get()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ handleAlertSchedulingFail(it) }
    }

    private fun handleAlertSchedulingFail(schedulingError: AlertSchedulingError) {
        error.value = schedulingError.error
    }
}