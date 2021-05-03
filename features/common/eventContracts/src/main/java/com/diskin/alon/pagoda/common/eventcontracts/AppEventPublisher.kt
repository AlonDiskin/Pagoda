package com.diskin.alon.pagoda.common.eventcontracts

interface AppEventPublisher<E : Any> {

    fun publish(event: E)
}