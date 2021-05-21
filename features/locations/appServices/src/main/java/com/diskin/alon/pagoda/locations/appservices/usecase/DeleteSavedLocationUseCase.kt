package com.diskin.alon.pagoda.locations.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.CoordinatesDto
import com.diskin.alon.pagoda.locations.appservices.model.DeleteSavedLocationRequest
import com.diskin.alon.pagoda.locations.domain.Coordinates
import io.reactivex.Single
import javax.inject.Inject

/**
 * Coordinate app operations to remove a location from user saved locations.
 */
class DeleteSavedLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val mapper: Mapper<DeleteSavedLocationRequest,Coordinates>
) : UseCase<DeleteSavedLocationRequest,Single<AppResult<Unit>>> {

    override fun execute(param: DeleteSavedLocationRequest): Single<AppResult<Unit>> {
        return repository.unSave(mapper.map(param))
    }
}