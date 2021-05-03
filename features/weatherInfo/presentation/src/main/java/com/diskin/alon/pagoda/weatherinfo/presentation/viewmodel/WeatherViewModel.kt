package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.presentation.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.CurrentLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.LocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherInfoModel
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
    @WeatherInfoModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    private val weatherSubject: BehaviorSubject<WeatherModelRequest> = initWeatherSubject()
    private val _weather = MutableLiveData<UiWeather>()
    val weather: LiveData<UiWeather> get() = _weather
    private val _update = MutableLiveData<UpdateViewData>(UpdateViewData.Refresh)
    val update: LiveData<UpdateViewData> get() = _update
    private val _error = MutableLiveData<ErrorViewData>()
    val error: LiveData<ErrorViewData> get() = _error
    val isCurrentLocation: Boolean = initIsCurrentLocation()

    init {
        // Add weather data subscription to view model
        addSubscription(createWeatherSubscription())
    }

    /**
     * Refresh the content of weather data.
     */
    fun refresh() {
        _error.value = ErrorViewData.NoError
        _update.value = UpdateViewData.Refresh

        weatherSubject.value?.let { weatherSubject.onNext(it) }
    }

    /**
     * Create rx subscription for model weather data,to be shown in view ui.
     */
    private fun createWeatherSubscription(): Disposable {
        return weatherSubject
            .switchMap { model.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleWeatherResult, this::handleWeatherSubscriptionError)
    }

    private fun handleWeatherResult(result: Result<UiWeather>) {
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

    private fun initWeatherSubject(): BehaviorSubject<WeatherModelRequest> {
        val request = when(savedState.contains(LOCATION_LAT) && savedState.contains(LOCATION_LON)) {
            true -> LocationWeatherModelRequest(
                savedState.get(LOCATION_LAT)!!,
                savedState.get(LOCATION_LON)!!
            )

            else -> CurrentLocationWeatherModelRequest
        }

        return BehaviorSubject.createDefault(request)
    }

    private fun initIsCurrentLocation(): Boolean {
        return !(savedState.contains(LOCATION_LAT) && savedState.contains(LOCATION_LON))
    }
}
