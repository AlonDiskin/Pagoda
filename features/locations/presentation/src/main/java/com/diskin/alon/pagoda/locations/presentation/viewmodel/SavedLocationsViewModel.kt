package com.diskin.alon.pagoda.locations.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.locations.presentation.model.SavedLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.util.LocationsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    @LocationsModel private val model: Model
) : RxViewModel() {

    private val _locations = MutableLiveData<PagingData<UiLocation>>()
    val locations: LiveData<PagingData<UiLocation>> get() = _locations

    init { addSubscription(createSavedLocationsSubscription()) }

    /**
     * Create rx subscription for model saved locations.
     */
    private fun createSavedLocationsSubscription(): Disposable {
        return model.execute(SavedLocationsModelRequest)
            .cachedIn(viewModelScope)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { paging -> _locations.value = paging }
    }
}