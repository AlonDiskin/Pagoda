package com.diskin.alon.pagoda.common.appservices

import io.reactivex.Observable

/**
 * Application use case contract.
 *
 * @param P use case input type.
 * @param R use case result type.
 */
interface AppUseCase<P : Any,R : Any> {

    /**
     * Execute use case.
     */
    fun execute(param: P): Observable<AppResult<R>>
}