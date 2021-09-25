package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocationWeatherRequest
import io.reactivex.Observable

object UserLocationWeatherModelRequest : ModelRequest<UserLocationWeatherRequest, Observable<AppResult<UiWeather>>>(UserLocationWeatherRequest())