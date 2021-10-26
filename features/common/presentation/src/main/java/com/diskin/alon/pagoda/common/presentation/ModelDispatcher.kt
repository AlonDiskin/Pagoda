package com.diskin.alon.pagoda.common.presentation

import com.diskin.alon.pagoda.common.appservices.usecase.UseCase
import com.diskin.alon.pagoda.common.util.Mapper

/**
 * Central dispatcher that serves requests from view models. This class handles
 * all needed operations to perform against the app services layer in order to
 * return the requested models for clients.
 *
 * @param map
 */
class ModelDispatcher(
    private val map: Map<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>
) : Model {

    override fun <P : Any, R : Any> execute(request: ModelRequest<P, R>): R {
        map[request::class.java]?.let { pair ->
            try {
                @Suppress("UNCHECKED_CAST")
                val useCase: UseCase<P, *> = pair.first as UseCase<P, *>
                val useCaseRes = useCase.execute(request.param)

                if (pair.second != null) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val mapper: Mapper<Any, R> = pair.second as Mapper<Any, R>
                        return mapper.map(useCaseRes)

                    } catch (e: Throwable) {
                        throw IllegalArgumentException("unmatched use case result mapper, for request:${request::class.java}")
                    }

                } else {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        return useCaseRes as R

                    } catch (e: Throwable) {
                        throw IllegalArgumentException(
                            "no mapper supplied for use case,return type must match expected" +
                                    " model request request:${request::class.java} result"
                        )
                    }
                }

            } catch (e: Throwable) {
                throw IllegalArgumentException("request:${request::class.java}, has no existing use case in dispatcher")
            }
        }.run {
            throw IllegalArgumentException("unknown request:${request::class.java}")
        }
    }
}