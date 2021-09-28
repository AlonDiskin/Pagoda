package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WorldLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UserLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherInfoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @WeatherInfoModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    private val weatherSubject: BehaviorSubject<ModelRequest<*,Observable<AppResult<UiWeather>>>> = initWeatherSubject()
    private val _weather = MutableLiveData<UiWeather>()
    val weather: LiveData<UiWeather> get() = _weather
    private val _update = MutableLiveData<UpdateViewData>()
    val update: LiveData<UpdateViewData> get() = _update
    val error = SingleLiveEvent<AppError>()
    val isCurrentLocation: Boolean = initIsCurrentLocation()

    init {
        // Add weather data subscription to view model
        addSubscription(createWeatherSubscription())
    }

    fun refresh() {
        weatherSubject.value?.let { weatherSubject.onNext(it) }
    }

    private fun createWeatherSubscription(): Disposable {
        return weatherSubject
            .switchMap { model.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleWeatherSubscriptionUpdate,this::handleWeatherSubscriptionError)
    }

    private fun handleWeatherSubscriptionUpdate(result: AppResult<UiWeather>) {
        when(result) {
            is AppResult.Success -> handleWeatherSuccessfulUpdate(result.data)
            is AppResult.Error -> handleWeatherUpdateError(result.error)
            is AppResult.Loading -> handleWeatherUpdateLoading()
        }
    }

    private fun handleWeatherSubscriptionError(error: Throwable?) {
        _update.value = UpdateViewData.EndRefresh
        error?.printStackTrace()
    }

    private fun handleWeatherSuccessfulUpdate(weather: UiWeather) {
        _update.value = UpdateViewData.EndRefresh
        _weather.value = weather
    }

    private fun handleWeatherUpdateError(resultError: AppError) {
        _update.value = UpdateViewData.EndRefresh
        error.value = resultError
    }

    private fun handleWeatherUpdateLoading() {
        _update.value = UpdateViewData.Refresh
    }

    private fun initWeatherSubject(): BehaviorSubject<ModelRequest<*,Observable<AppResult<UiWeather>>>> {
        val request = when(savedState.contains(ARG_LAT) && savedState.contains(ARG_LON)) {
            true -> WorldLocationWeatherModelRequest(
                savedState.get<Float>(ARG_LAT)!!.toString().toDouble(),
                savedState.get<Float>(ARG_LON)!!.toString().toDouble()
            )

            else -> UserLocationWeatherModelRequest
        }

        return BehaviorSubject.createDefault(request)
    }

    private fun initIsCurrentLocation(): Boolean {
        return !(savedState.contains(ARG_LAT) && savedState.contains(ARG_LON))
    }
}
