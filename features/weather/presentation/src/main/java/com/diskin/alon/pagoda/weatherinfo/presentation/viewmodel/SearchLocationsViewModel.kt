package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.weatherinfo.presentation.model.BookmarkLocationModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.SearchLocationsModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocationSearchResult
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class SearchLocationsViewModel @Inject constructor(
    @WeatherModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    companion object {
        const val KEY_QUERY = "search_query"
        const val DEFAULT_QUERY = ""
    }

    private val bookmarkSubject = BehaviorSubject.create<UiLocationSearchResult>()
    private val _query: BehaviorSubject<String> = BehaviorSubject.createDefault(getQueryState() ?: DEFAULT_QUERY)
    val query: String get() = _query.value!!
    private val _results = MutableLiveData<PagingData<UiLocationSearchResult>>()
    val results: LiveData<PagingData<UiLocationSearchResult>> get() = _results

    init {
        addSubscription(createQuerySubscription())
        addSubscription(createBookmarkingSubscription())
    }

    /**
     * Create rx chain for performing search against model from query values
     */
    private fun createQuerySubscription(): Disposable {
        return _query
            .switchMap { model.execute(SearchLocationsModelRequest(it))}
            .cachedIn(viewModelScope)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { paging -> _results.value = paging }
    }

    private fun createBookmarkingSubscription(): Disposable {
        return bookmarkSubject
            .concatMapSingle { model.execute(BookmarkLocationModelRequest(it.lat,it.lon)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    /**
     * Perform world locations search.
     */
    fun search(query: String) {
        storeQueryState(query)
        _query.onNext(query)
    }

    /**
     * Add location result to users bookmarked location.
     */
    fun addToBookmarked(location: UiLocationSearchResult) {
        bookmarkSubject.onNext(location)
    }

    private fun storeQueryState(query: String) {
        savedState.set(KEY_QUERY,query)
    }

    private fun getQueryState(): String? = savedState.get(KEY_QUERY)
}