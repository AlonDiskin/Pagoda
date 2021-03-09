package com.diskin.alon.pagoda.util

import okio.Buffer
import okio.Okio
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtil {
    fun readStringFromFile(resourceName: String): String {
        val stream = javaClass.classLoader!!.getResourceAsStream(resourceName)
        val reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
        }

        reader.close()
        return builder.toString()
    }

    fun readImageIntoBuffer(resourceName: String): Buffer {
        val buffer = Buffer()
        val stream = javaClass.classLoader!!
            .getResourceAsStream(resourceName)

        buffer.writeAll(Okio.source(stream))

        return buffer
    }
}