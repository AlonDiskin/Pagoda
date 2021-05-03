package com.diskin.alon.pagoda.common.eventcontracts

import io.reactivex.Observable

interface AppEventProvider<E : Any> {

    fun get(): Observable<E>
}