package com.diskin.alon.pagoda.weather.infrastructure

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.shared.AppDataPublisher
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import com.diskin.alon.pagoda.weatherinfo.appservices.model.AlertInfoDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.ScheduleWeatherAlertNotificationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.ScheduleWeatherAlertNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@OptionalInject
@AndroidEntryPoint
class WeatherAlertSettingHandlerService : Service() {

    @Inject
    lateinit var schedulingUseCase: ScheduleWeatherAlertNotificationUseCase
    @Inject
    lateinit var alertProvider: AppDataProvider<Observable<WeatherAlertEnabled>>
    @Inject
    lateinit var errorPublisher: AppDataPublisher<AlertSchedulingError>

    private val subscriptions = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        subscribeToAlertEnabling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAlertSubscription()
    }

    private fun subscribeToAlertEnabling() {
        subscriptions.add(createAlertEnablingSubscription())
    }

    private fun cancelAlertSubscription() {
        if (!subscriptions.isDisposed) {
            subscriptions.dispose()
        }
    }

    private fun createAlertEnablingSubscription(): Disposable {
        return alertProvider.get()
            .switchMapSingle {
                schedulingUseCase.execute(
                    ScheduleWeatherAlertNotificationRequest(AlertInfoDto(it.enabled))
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleSchedulingResult,this::handleSchedulingSubscriptionError)
    }

    private fun handleSchedulingResult(result: AppResult<Unit>) {
        when(result) {
            is AppResult.Error -> errorPublisher.publish(AlertSchedulingError(result.error))
        }
    }

    private fun handleSchedulingSubscriptionError(error: Throwable) {
        errorPublisher.publish(AlertSchedulingError(AppError(ErrorType.UNKNOWN_ERR,error)))
    }
}