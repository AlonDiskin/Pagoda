package com.diskin.alon.pagoda.weatherinfo.presentation.model

data class UiLocationSearchResult(val lat: Double,
                                  val lon: Double,
                                  val name: String,
                                  val country: String,
                                  val bookmarked: Boolean)