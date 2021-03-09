package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import io.reactivex.Observable

/**
 * App unit preference provider contract.
 */
interface AppPrefsStore {

    /**
     * Get the weather unit system pref .
     */
    fun getUnitSystem(): Observable<Result<UnitSystem>>
}