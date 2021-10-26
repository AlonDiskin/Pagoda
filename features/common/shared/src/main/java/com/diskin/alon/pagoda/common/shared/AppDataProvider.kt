package com.diskin.alon.pagoda.common.shared

interface AppDataProvider<E : Any> {

    fun get(): E
}