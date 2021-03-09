package com.diskin.alon.pagoda.common.appservices

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function

sealed class Result<T : Any> {

    data class Success<T : Any>(val data: T) : Result<T>()

    data class Error<T : Any>(val error: AppError) : Result<T>()
}

data class AppError(val description: String, val retriable: Boolean,val origin: Throwable? = null): Throwable()

fun <T : Any, R : Any> Observable<Result<T>>.mapResult(mapper: Function<T,R>): Observable<Result<R>> {
    return this.map {
        when(it) {
            is Result.Success -> Result.Success(
                mapper.apply(
                    it.data
                )
            )
            is Result.Error -> Result.Error(it.error)
        }
    }
}

fun <T : Any> Observable<T>.toResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<Result<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

fun <T : Any> Single<T>.toSingleResult(errorHandler: ((Throwable) -> (AppError))? = null): Single<Result<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

fun <T : Any> Observable<Result<T>>.toData(): Observable<T> {
    return this.flatMap {
        when(it ) {
            is Result.Success -> Observable.just(it.data)
            is Result.Error -> Observable.error(it.error)
        }
    }
}

fun <T : Any> Single<Result<T>>.toSingleData(): Single<T> {
    return this.flatMap {
        when(it ) {
            is Result.Success -> Single.just(it.data)
            is Result.Error -> Single.error(it.error)
        }
    }
}

private fun <T : Any> toSuccessResult(data: T): Result<T> {
    return Result.Success(data)
}

private fun <T : Any> toResultError(throwable: Throwable,errorHandler: ((Throwable) -> (AppError))? = null): Result<T> {
    return when(throwable) {
        is AppError -> Result.Error(throwable)
        else -> Result.Error(
            errorHandler?.invoke(throwable) ?: AppError("Unknown error", false)
        )
    }
}