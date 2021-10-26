package com.diskin.alon.pagoda.common.shared

interface AppDataPublisher<E : Any> {

    fun publish(date: E)
}