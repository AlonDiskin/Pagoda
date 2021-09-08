package com.diskin.alon.pagoda.common.appservices

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

sealed class Result<T : Any> {

    data class Success<T : Any>(val data: T) : Result<T>()

    data class Error<T : Any>(val error: AppError) : Result<T>()
}

sealed class AppResult<T : Any> {

    data class Success<T : Any>(val data: T) : AppResult<T>()

    data class Error<T : Any>(val error: AppError) : AppResult<T>()

    class Loading<T : Any> : AppResult<T>()
}

sealed class AppEvent {

    object Success : AppEvent()

    data class Error(val error: AppError) : AppEvent()

    object Loading : AppEvent()
}

enum class ErrorType{
    UNKNOWN_ERR,DEVICE_NETWORK,REMOTE_SERVER,DEVICE_LOCATION,LOCATION_PERMISSION,
    LOCATION_BACKGROUND_PERMISSION,DB_ERROR
}

data class AppError(val type: ErrorType,val origin: Throwable? = null): Throwable()

fun <T : Any, R : Any> Single<AppResult<T>>.mapAppResult(mapper: Function<T,R>): Single<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            is AppResult.Error -> AppResult.Error(it.error)
            is AppResult.Loading -> AppResult.Loading()
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.mapAppResult(mapper: Function<T,R>): Observable<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            is AppResult.Error -> AppResult.Error(it.error)
            is AppResult.Loading -> AppResult.Loading()
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.flatMapAppResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(AppResult.Error(it.error))
            is AppResult.Loading -> Observable.just(AppResult.Loading())
        }
    }
}

fun <T : Any, R : Any> Single<AppResult<T>>.flatMapAppResult(mapper: (T) -> (Single<AppResult<R>>)): Single<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Single.just(AppResult.Error(it.error))
            is AppResult.Loading -> Single.just(AppResult.Loading())
        }
    }
}

fun <T : Any, R : Any> Single<Result<T>>.flatMapResult(mapper: (T) -> (Single<Result<R>>)): Single<Result<R>> {
    return this.flatMap {
        when(it) {
            is Result.Success -> mapper.invoke(it.data)
            is Result.Error -> Single.just(Result.Error(it.error))
        }
    }
}

fun <T : Any, R : Any> Observable<Result<T>>.flatMapResult(mapper: (T) -> (Observable<Result<R>>)): Observable<Result<R>> {
    return this.flatMap {
        when(it) {
            is Result.Success -> mapper.invoke(it.data)
            is Result.Error -> Observable.just(Result.Error(it.error))
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.switchMapAppResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.switchMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(AppResult.Error(it.error))
            is AppResult.Loading -> Observable.just(AppResult.Loading())
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

fun <T : Any> Observable<Result<T>>.toAppResult(): Observable<AppResult<T>> {
    return this.map {
        when(it) {
            is Result.Success -> AppResult.Success(it.data)
            is Result.Error -> AppResult.Error(it.error)
        }
    }
}

fun <T : Any> Observable<Result<T>>.toLoadingAppResult(): Observable<AppResult<T>> {
    return this.map {
        when(it) {
            is Result.Success -> AppResult.Success(it.data)
            is Result.Error -> AppResult.Error(it.error)
        }
    }.startWith(AppResult.Loading())
}

fun <T : Any> Single<Result<T>>.singleResultToAppResult(): Observable<AppResult<T>> {
    return this.toObservable().map {
        when(it) {
            is Result.Success -> AppResult.Success(it.data)
            is Result.Error -> AppResult.Error(it.error)
        }
    }
}

fun <T : Any> Single<Result<T>>.singleResultToLoadingAppResult(): Observable<AppResult<T>> {
    return this.toObservable().map {
        when(it) {
            is Result.Success -> AppResult.Success(it.data)
            is Result.Error -> AppResult.Error(it.error)
        }
    }.startWith(AppResult.Loading())
}

fun <T : Any> Single<Result<T>>.toAppResult(): Single<AppResult<T>> {
    return this.map {
        when(it) {
            is Result.Success -> AppResult.Success(it.data)
            is Result.Error -> AppResult.Error(it.error)
        }
    }
}

fun <T : Any> Observable<T>.toAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
}

fun <T : Any> Observable<T>.toIOLoadingAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.subscribeOn(Schedulers.io()).map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
        .startWith(AppResult.Loading())
}

fun <T : Any> Single<T>.toSingleAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Single<AppResult<T>> {
    return this.map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
}

fun <T : Any> Observable<Result<T>>.toData(): Observable<T> {
    return this.flatMap {
        when(it ) {
            is Result.Success -> Observable.just(it.data)
            is Result.Error -> Observable.error(it.error)
        }
    }
}
//
//fun <T : Any> Single<AppResult<T>>.toSingleData(): Single<T> {
//    return this.flatMap {
//        when(it) {
//            is AppResult.Success -> Single.just(it.data)
//            is AppResult.Error -> Single.error(it.error)
//        }
//    }
//}

private fun <T : Any> toSuccessAppResult(data: T): AppResult<T> {
    return AppResult.Success(data)
}

private fun <T : Any> toAppResultError(throwable: Throwable, errorHandler: ((Throwable) -> (AppError))? = null): AppResult<T> {
    return when(throwable) {
        is AppError -> AppResult.Error(throwable)
        else -> AppResult.Error(
            errorHandler?.invoke(throwable) ?: AppError(ErrorType.UNKNOWN_ERR)
        )
    }
}

private fun <T : Any> toSuccessResult(data: T): Result<T> {
    return Result.Success(data)
}

private fun <T : Any> toResultError(throwable: Throwable, errorHandler: ((Throwable) -> (AppError))? = null): Result<T> {
    return when(throwable) {
        is AppError -> Result.Error(throwable)
        else -> Result.Error(
            errorHandler?.invoke(throwable) ?: AppError(ErrorType.UNKNOWN_ERR)
        )
    }
}