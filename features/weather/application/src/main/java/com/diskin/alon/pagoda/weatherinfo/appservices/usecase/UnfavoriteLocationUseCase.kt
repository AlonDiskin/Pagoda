package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnfavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import io.reactivex.Single
import javax.inject.Inject

/**
 * Coordinate app operations to remove a location from user saved locations.
 */
class UnfavoriteLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val mapper: Mapper<UnfavoriteLocationRequest, Coordinates>
) : UseCase<UnfavoriteLocationRequest,Single<AppResult<Unit>>> {

    override fun execute(param: UnfavoriteLocationRequest): Single<AppResult<Unit>> {
        return repository.unfavorite(mapper.map(param))
    }
}