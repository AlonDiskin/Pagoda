package com.diskin.alon.pagoda.common.appservices

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function

sealed class AppResult<T : Any> {

    data class Success<T : Any>(val data: T) : AppResult<T>()

    data class Error<T : Any>(val error: AppError) : AppResult<T>()
}

enum class ErrorType{
    UNKNOWN_ERR,DEVICE_NETWORK,REMOTE_SERVER,DEVICE_LOCATION,LOCATION_PERMISSION,
    LOCATION_BACKGROUND_PERMISSION
}

data class AppError(val type: ErrorType,val origin: Throwable? = null): Throwable()

fun <T : Any, R : Any> Observable<AppResult<T>>.mapResult(mapper: Function<T,R>): Observable<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            is AppResult.Error -> AppResult.Error(it.error)
        }
    }
}

fun <T : Any> Observable<T>.toResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

fun <T : Any> Single<T>.toSingleResult(errorHandler: ((Throwable) -> (AppError))? = null): Single<AppResult<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

fun <T : Any> Observable<AppResult<T>>.toData(): Observable<T> {
    return this.flatMap {
        when(it ) {
            is AppResult.Success -> Observable.just(it.data)
            is AppResult.Error -> Observable.error(it.error)
        }
    }
}

fun <T : Any> Single<AppResult<T>>.toSingleData(): Single<T> {
    return this.flatMap {
        when(it ) {
            is AppResult.Success -> Single.just(it.data)
            is AppResult.Error -> Single.error(it.error)
        }
    }
}

private fun <T : Any> toSuccessResult(data: T): AppResult<T> {
    return AppResult.Success(data)
}

private fun <T : Any> toResultError(throwable: Throwable,errorHandler: ((Throwable) -> (AppError))? = null): AppResult<T> {
    return when(throwable) {
        is AppError -> AppResult.Error(throwable)
        else -> AppResult.Error(
            errorHandler?.invoke(throwable) ?: AppError(ErrorType.UNKNOWN_ERR)
        )
    }
}