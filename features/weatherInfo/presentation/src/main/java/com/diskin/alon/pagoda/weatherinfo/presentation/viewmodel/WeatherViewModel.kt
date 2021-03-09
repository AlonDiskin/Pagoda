package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.presentation.ErrorViewData
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.model.CurrentWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Weather UI view model.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val model: Model
) : RxViewModel() {

    private val weatherSubject = BehaviorSubject.createDefault(Unit)
    private val _weather = MutableLiveData<LocationWeatherDto>()
    val weather: LiveData<LocationWeatherDto> get() = _weather
    private val _update = MutableLiveData<UpdateViewData>(UpdateViewData.Refresh)
    val update: LiveData<UpdateViewData> get() = _update
    private val _error = MutableLiveData<ErrorViewData>()
    val error: LiveData<ErrorViewData> get() = _error

    init {
        addSubscription(createWeatherSubscription())
    }

    fun refresh() {
        _error.value = ErrorViewData.NoError
        _update.value = UpdateViewData.Refresh
        weatherSubject.onNext(Unit)
    }

    private fun createWeatherSubscription(): Disposable {
        return weatherSubject
            .switchMap { model.execute(CurrentWeatherModelRequest()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleWeatherResult, this::handleWeatherSubscriptionError)
    }

    private fun handleWeatherResult(result: Result<LocationWeatherDto>) {
        _update.value = UpdateViewData.EndRefresh
        when(result) {
            is Result.Success -> _weather.value = result.data
            is Result.Error -> _error.value = ErrorViewData.Error(result.error)
        }
    }

    private fun handleWeatherSubscriptionError(error: Throwable?) {
        _update.value = UpdateViewData.EndRefresh
        error?.printStackTrace()
    }
}
