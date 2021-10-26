package com.diskin.alon.pagoda.weather.infrastructure

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.shared.AppDataPublisher
import com.diskin.alon.pagoda.weather.shared.AlertSchedulingError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Provide [AlertSchedulingError] events for subscribers.
 */
class AlertScheduleErrorDataHandler @Inject constructor() :
    AppDataPublisher<AlertSchedulingError>, AppDataProvider<Observable<AlertSchedulingError>> {

    private val eventSubject = PublishSubject.create<AlertSchedulingError>()

    override fun publish(date: AlertSchedulingError) {
        eventSubject.onNext(date)
    }

    override fun get(): Observable<AlertSchedulingError> {
        return eventSubject
    }
}