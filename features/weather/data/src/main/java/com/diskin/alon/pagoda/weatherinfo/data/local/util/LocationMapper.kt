package com.diskin.alon.pagoda.weatherinfo.data.local.util

import android.app.Application
import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class LocationMapper @Inject constructor(private val app: Application) : Mapper<PagingData<LocationEntity>,PagingData<Location>> {
    override fun map(source: PagingData<LocationEntity>): PagingData<Location> {
        return source.map {
            Location(
                Coordinates(it.lat,it.lon),
                it.name,
                mapLocationCountryCode(it.country),
                it.state,
                it.bookmarked
            )
        }
    }

    private fun mapLocationCountryCode(code: String): String {
        val json = loadJSONFromAsset()
        val arr = JSONArray(json)
        var res = ""

        for (i in 0 until arr.length()) {
            val countryCode = arr.getJSONObject(i).getString("code")

            if (countryCode == code) {
                res = arr.getJSONObject(i).getString("name")
                break
            }
        }

        return if(res.isEmpty()) code else res
    }

    private fun loadJSONFromAsset(): String? {
        return try {
            val input: InputStream = app.assets.open("country_codes.json")
            val size: Int = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }
}