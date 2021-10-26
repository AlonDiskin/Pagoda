package com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.RxViewModel
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.weatherinfo.presentation.model.*
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    @WeatherModel private val model: Model,
    private val savedState: SavedStateHandle
) : RxViewModel(){

    companion object {
        @VisibleForTesting
        const val KEY_QUERY = "search_query"
        @VisibleForTesting
        const val DEFAULT_QUERY = ""
        @VisibleForTesting
        const val KEY_SEARCH_EXPANDED = "search_view_expanded"
        @VisibleForTesting
        const val DEFAULT_SEARCH_EXPANDED = false
    }

    var searchViewExpanded: Boolean
    set(value) { savedState[KEY_SEARCH_EXPANDED] = value }
    get() = savedState[KEY_SEARCH_EXPANDED] ?: DEFAULT_SEARCH_EXPANDED

    private val _query: BehaviorSubject<String> = BehaviorSubject.createDefault(
        savedState.get(KEY_QUERY) ?: DEFAULT_QUERY)
    val query: String get() = _query.value!!

    private val _locations = MutableLiveData<PagingData<UiLocation>>()
    val locations: LiveData<PagingData<UiLocation>> get() = _locations

    private val favoritingSubject = BehaviorSubject.create<FavoritingAction>()
    val error = SingleLiveEvent<AppError>()

    init {
        addSubscription(
            createLocationsSubscription(),
            createFavoritingSubscription()
        )
    }

    private fun createLocationsSubscription(): Disposable {
        return _query
            .switchMap {
                when(it.isEmpty()) {
                    true -> model.execute(FavoriteLocationsModelRequest)
                    false -> model.execute(SearchLocationsModelRequest(it))
                }
            }
            .cachedIn(viewModelScope)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { paging -> _locations.value = paging }
    }

    private fun createFavoritingSubscription(): Disposable {
        return favoritingSubject
            .concatMapSingle {
                when(it) {
                    is FavoritingAction.Add -> model.execute(FavoriteLocationModelRequest(it.lat,it.lon))
                    is FavoritingAction.Remove -> model.execute(UnfavoriteLocationModelRequest(it.lat,it.lon))
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun favoriteLocation(location: UiLocation) {
        favoritingSubject.onNext(FavoritingAction.Add(location.lat,location.lon))
    }

    fun unfavoriteLocation(location: UiLocation) {
        favoritingSubject.onNext(FavoritingAction.Remove(location.lat,location.lon))
    }

    fun search(query: String) {
        savedState.set(KEY_QUERY,query)
        _query.onNext(query)
    }

    fun loadFavorites() {
        savedState.set(KEY_QUERY,"")
        _query.onNext("")
    }
}