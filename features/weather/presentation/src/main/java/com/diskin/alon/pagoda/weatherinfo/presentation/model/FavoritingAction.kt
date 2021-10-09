package com.diskin.alon.pagoda.weatherinfo.presentation.model

sealed class FavoritingAction {

    data class Add(val lat: Double, val lon: Double): FavoritingAction()

    data class Remove(val lat: Double, val lon: Double): FavoritingAction()
}