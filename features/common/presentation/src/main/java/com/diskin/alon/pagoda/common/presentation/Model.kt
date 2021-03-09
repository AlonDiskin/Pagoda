package com.diskin.alon.pagoda.common.presentation

/**
 * Provides an application data servicing interface for view models.
 */
interface Model {

    /**
     * Execute application related operations,to serve the given [request] for the desired result.
     *
     * @param request data needed for a specific application operation..
     * @return the specified type from given [request].
     */
    fun <P : Any,R : Any> execute(request: ModelRequest<P, R>): R
}