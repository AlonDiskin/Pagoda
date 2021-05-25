package com.diskin.alon.pagoda.locations.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.locations.presentation.model.DeleteSavedLocationModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.SavedLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.util.LocationsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    @LocationsModel private val model: Model
) : RxViewModel() {

    private val deleteLocationSubject = BehaviorSubject.create<UiLocation>()
    private val _locations = MutableLiveData<PagingData<UiLocation>>()
    val locations: LiveData<PagingData<UiLocation>> get() = _locations
    val error = SingleLiveEvent<AppError>()

    init {
        addSubscription(createSavedLocationsSubscription())
        addSubscription(createLocationDeletionSubscription())
    }

    /**
     * Create rx subscription for model saved locations.
     */
    private fun createSavedLocationsSubscription(): Disposable {
        return model.execute(SavedLocationsModelRequest)
            .cachedIn(viewModelScope)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { paging -> _locations.value = paging }
    }

    /**
     * Create rx subscription for saved location deletion operation.
     */
    private fun createLocationDeletionSubscription():  Disposable {
        return deleteLocationSubject
            .concatMapSingle { model.execute(DeleteSavedLocationModelRequest(it.lat,it.lon)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleDeleteLocationResult,::handleDeleteSubscriptionError)
    }

    fun deleteSavedLocation(location: UiLocation) {
        deleteLocationSubject.onNext(location)
    }

    private fun handleDeleteLocationResult(result: AppResult<Unit>) {
        when(result) {
            is AppResult.Error -> error.value = result.error
        }
    }

    private fun handleDeleteSubscriptionError(e: Throwable) {
        error.value = AppError(ErrorType.UNKNOWN_ERR)
        e.printStackTrace()
    }
}