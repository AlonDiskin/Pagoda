package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.model.LocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.LocationWeatherModelRequest.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @WeatherModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    companion object {
        @VisibleForTesting
        const val ARG_COORDINATES = "coordinates"
    }

    @VisibleForTesting
    data class CoordinatesState(val lat: Double,val lon: Double) : Serializable

    private val weatherSubject: BehaviorSubject<LocationWeatherModelRequest>
    private val _weather = MutableLiveData<UiWeather>()
    val weather: LiveData<UiWeather> get() = _weather
    private val _update = MutableLiveData<UpdateViewData>()
    val update: LiveData<UpdateViewData> get() = _update
    val error = SingleLiveEvent<AppError>()
    private val _isCurrentLocationWeather = MutableLiveData<Boolean>()
    val isCurrentLocationWeather: LiveData<Boolean> get() = _isCurrentLocationWeather

    init {
        // Init weather subject
        val state = savedState.get<CoordinatesState>(ARG_COORDINATES)
        weatherSubject = when(state) {
            null -> BehaviorSubject.createDefault(UserLocationWeatherModelRequest)
            else -> BehaviorSubject.createDefault(WorldLocationWeatherModelRequest(state.lat, state.lon))
        }

        // Add weather data subscription to view model
        addSubscription(createWeatherSubscription())
    }

    fun refresh() {
        weatherSubject.value?.let { weatherSubject.onNext(it) }
    }

    fun loadCurrentLocationWeather() {
        savedState[ARG_COORDINATES] = null
        weatherSubject.onNext(UserLocationWeatherModelRequest)
    }

    fun loadLocationWeather(lat: Double, lon: Double) {
        savedState[ARG_COORDINATES] = CoordinatesState(lat, lon)
        weatherSubject.onNext(WorldLocationWeatherModelRequest(lat, lon))
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

        _isCurrentLocationWeather.value = when(weatherSubject.value) {
            is UserLocationWeatherModelRequest -> true
            else -> false
        }
    }

    private fun handleWeatherUpdateError(resultError: AppError) {
        _update.value = UpdateViewData.EndRefresh
        error.value = resultError
    }

    private fun handleWeatherUpdateLoading() {
        _update.value = UpdateViewData.Refresh
    }
}
