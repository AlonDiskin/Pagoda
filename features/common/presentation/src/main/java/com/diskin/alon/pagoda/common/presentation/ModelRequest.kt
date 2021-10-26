package com.diskin.alon.pagoda.common.presentation

/**
 * Holds the data needed for the model to handle requests for application data and operations,
 * to the presentation view models.
 *
 * @param P use case param type
 * @param R model result type
 */
abstract class ModelRequest<P : Any,R : Any>(val param: P)