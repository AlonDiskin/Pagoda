package com.diskin.alon.pagoda.common.featuretesting

import java.io.File

fun getJsonFromResource(resource: String): String {
    val topLevelClass = object : Any() {}.javaClass.enclosingClass!!
    val jsonResource = topLevelClass.classLoader!! // javaClass.classLoader
        .getResource(resource)

    return File(jsonResource.toURI()).readText()
}