package com.diskin.alon.pagoda.locations.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.presentation.model.SearchModelRequest
import com.diskin.alon.pagoda.locations.presentation.util.SearchLocationsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class SearchLocationViewModel @Inject constructor(
    @SearchLocationsModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    companion object {
        const val KEY_QUERY = "search_query"
        const val DEFAULT_QUERY = ""
    }

    private val _query: BehaviorSubject<String> = BehaviorSubject.createDefault(getQueryState() ?: DEFAULT_QUERY)
    val query: String get() = _query.value!!
    private val _results = MutableLiveData<PagingData<LocationSearchResult>>()
    val results: LiveData<PagingData<LocationSearchResult>> get() = _results

    init { addSubscription(createQuerySubscription()) }

    /**
     * Create rx chain for performing search against model from query values
     */
    private fun createQuerySubscription(): Disposable {
        return _query
            .switchMap { model.execute(SearchModelRequest(it))}
            .cachedIn(viewModelScope)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { paging -> _results.value = paging }
    }

    /**
     * Perform world locations search.
     */
    fun search(query: String) {
        storeQueryState(query)
        _query.onNext(query)
    }

    private fun storeQueryState(query: String) {
        savedState.set(KEY_QUERY,query)
    }

    private fun getQueryState(): String? = savedState.get(KEY_QUERY)
}