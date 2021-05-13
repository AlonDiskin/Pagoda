package com.diskin.alon.pagoda.settings.data.remote

import com.diskin.alon.pagoda.settings.data.BuildConfig

const val OPEN_WEATHER_API = "https://api.openweathermap.org/"
const val LAT_PARAM = "lat"
const val LON_PARAM = "lon"
const val ALERT_DATA_PATH = "data/2.5/onecall"
const val ALERT_DATA_QUERY = "appid=${BuildConfig.OPEN_WEATHER_MAP_API_KEY}&units=metric&exclude=current,minutely,hourly,daily"
const val LOCATION_PATH = "geo/1.0/reverse"
const val LOCATION_QUERY = "appid=${BuildConfig.OPEN_WEATHER_MAP_API_KEY}&limit=1"