package com.diskin.alon.pagoda.locations.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.BookmarkLocationRequest
import com.diskin.alon.pagoda.locations.domain.Coordinates
import io.reactivex.Single
import javax.inject.Inject

class BookmarkLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val requestMapper: Mapper<BookmarkLocationRequest,Coordinates>
) : UseCase<BookmarkLocationRequest,Single<AppResult<Unit>>> {

    override fun execute(param: BookmarkLocationRequest): Single<AppResult<Unit>> {
        return repository.bookmark(requestMapper.map(param))
    }
}