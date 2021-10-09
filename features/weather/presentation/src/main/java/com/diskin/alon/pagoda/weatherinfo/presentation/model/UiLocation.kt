package com.diskin.alon.pagoda.weatherinfo.presentation.model

data class UiLocation(val lat: Double,
                      val lon: Double,
                      val name: String,
                      val country: String,
                      val isFavorite: Boolean)