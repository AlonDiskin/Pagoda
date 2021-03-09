package com.diskin.alon.pagoda.common.util

/**
 * Models mapper contract.
 *
 * @param S1 first source model type.
 * @param S2 second source model type.
 * @param D destination model type.
 */
interface Mapper2<S1,S2,D : Any> {

    /**
     * Maps source model to destination type model.
     */
    fun map(source1: S1,source2: S2): D
}