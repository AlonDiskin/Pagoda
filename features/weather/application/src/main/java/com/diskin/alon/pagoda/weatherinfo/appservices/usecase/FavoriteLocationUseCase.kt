package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.FavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import io.reactivex.Single
import javax.inject.Inject

class FavoriteLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val requestMapper: Mapper<FavoriteLocationRequest, Coordinates>
) : UseCase<FavoriteLocationRequest,Single<AppResult<Unit>>> {

    override fun execute(param: FavoriteLocationRequest): Single<AppResult<Unit>> {
        return repository.favorite(requestMapper.map(param))
    }
}