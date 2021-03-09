package com.diskin.alon.pagoda.common.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Base class for [ViewModel] implementations that subscribe to Rx observables.
 */
abstract class RxViewModel : ViewModel() {

    private val container = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        // Dispose all rx subscriptions
        if (!container.isDisposed) {
            container.dispose()
        }
    }

    protected fun addSubscription(disposable: Disposable) {
        container.add(disposable)
    }
}