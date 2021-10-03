package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnBookmarkLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import io.reactivex.Single
import javax.inject.Inject

/**
 * Coordinate app operations to remove a location from user saved locations.
 */
class UnBookmarkLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val mapper: Mapper<UnBookmarkLocationRequest, Coordinates>
) : UseCase<UnBookmarkLocationRequest,Single<AppResult<Unit>>> {

    override fun execute(param: UnBookmarkLocationRequest): Single<AppResult<Unit>> {
        return repository.unBookmark(mapper.map(param))
    }
}