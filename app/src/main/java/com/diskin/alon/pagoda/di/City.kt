package com.diskin.alon.pagoda.di

data class City(val name: String,
                val state: String,
                val country: String,
                val coord: Coord) {

    data class Coord(val lat: Double, val lon: Double)
}