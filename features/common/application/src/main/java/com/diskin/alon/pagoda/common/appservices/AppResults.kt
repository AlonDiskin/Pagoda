package com.diskin.alon.pagoda.common.appservices

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

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

fun <T : Any, R : Any> Single<AppResult<T>>.mapResult(mapper: Function<T,R>): Single<AppResult<R>> {
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

fun <T : Any, R : Any> Observable<AppResult<T>>.mapResult(mapper: Function<T,R>): Observable<AppResult<R>> {
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

fun <T : Any, R : Any> Observable<AppResult<T>>.flatMapResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(AppResult.Error(it.error))
            is AppResult.Loading -> Observable.just(AppResult.Loading())
        }
    }
}

fun <T : Any, R : Any> Single<AppResult<T>>.flatMapResult(mapper: (T) -> (Single<AppResult<R>>)): Single<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Single.just(AppResult.Error(it.error))
            is AppResult.Loading -> Single.just(AppResult.Loading())
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.switchMapResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.switchMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(AppResult.Error(it.error))
            is AppResult.Loading -> Observable.just(AppResult.Loading())
        }
    }
}

fun <T : Any> Observable<T>.toResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

fun <T : Any> Observable<T>.toIOLoadingResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.subscribeOn(Schedulers.io()).map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
        .startWith(AppResult.Loading())
}

fun <T : Any> Single<T>.toSingleResult(errorHandler: ((Throwable) -> (AppError))? = null): Single<AppResult<T>> {
    return this.map { toSuccessResult(it) }
        .onErrorReturn { toResultError(it,errorHandler) }
}

//fun <T : Any> Observable<AppResult<T>>.toData(): Observable<T> {
//    return this.flatMap {
//        when(it ) {
//            is AppResult.Success -> Observable.just(it.data)
//            is AppResult.Error -> Observable.error(it.error)
//        }
//    }
//}
//
//fun <T : Any> Single<AppResult<T>>.toSingleData(): Single<T> {
//    return this.flatMap {
//        when(it) {
//            is AppResult.Success -> Single.just(it.data)
//            is AppResult.Error -> Single.error(it.error)
//        }
//    }
//}

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