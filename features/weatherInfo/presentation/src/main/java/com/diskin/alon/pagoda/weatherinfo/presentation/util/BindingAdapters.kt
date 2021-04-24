package com.diskin.alon.pagoda.weatherinfo.presentation.util

import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UvIndexDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherConditionDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDescriptionDto.*
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("setWeatherIcon")
fun setWeatherIcon(imageView: ImageView, conditionDto: WeatherConditionDto?) {
    conditionDto?.let {
        when(it.description) {
            Thunderstorm -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_thunder_96)
            }

            Drizzle -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_drizzle_96)
            }

            Rain -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_rain_96)
            }

            Snow -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_snow_96)
            }

            Mist, Fog -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_fog_96)
            }

            Clear -> {
                when(it.isDay) {
                    true -> ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clear_day_96)
                    else -> ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clear_night_96)
                }
            }

            Clouds -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clouds_96)
            }

            Haze, Dust, Sand -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_haze_96)
            }

            Tornado -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_tornado_96)
            }

            Unknown -> {
                ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_unknown_96)
            }
        }
    }
}

@BindingAdapter("setTemp")
fun setTemp(tv: TextView, temp: Double?) {
    temp?.let {
        val context = tv.context!!
        tv.text = context.getString(R.string.temp,it.toInt())
    }
}

@BindingAdapter("setDayOfWeek")
fun setDayOfWeek(tv: TextView, day: Int?) {
    day?.let {
        val context = tv.context!!
        tv.text = when(it) {
            1 -> context.getString(R.string.week_day_sunday)
            2 -> context.getString(R.string.week_day_monday)
            3 -> context.getString(R.string.week_day_tuesday)
            4 -> context.getString(R.string.week_day_wednesday)
            5 -> context.getString(R.string.week_day_thursday)
            6 -> context.getString(R.string.week_day_friday)
            7 -> context.getString(R.string.week_day_saturday)
            else -> throw IllegalArgumentException("Wrong day of week arg:$day")
        }
    }
}

@BindingAdapter("setHour")
fun setHour(tv: TextView, hour: Int?) {
    hour?.let {
        val context = tv.context!!
        tv.text = context.getString(R.string.hour,it)
    }
}

@BindingAdapter("setClockTimeZone")
fun setClockTimeZone(tc: TextClock, timeZone: String?) {
    timeZone?.let { tc.timeZone = it }
}

@BindingAdapter("setFeelTemp")
fun setFeelTemp(tv: TextView, temp: Double?) {
    temp?.let {
        val context = tv.context!!
        tv.text = context.getString(R.string.feel_temp,it.toInt())
    }
}

@BindingAdapter("setHumidity")
fun setHumidity(tv: TextView, humidity: Double?) {
    humidity?.let {
        tv.text = it.toInt().toString().plus("%")
    }
}

@BindingAdapter("setWindSpeed")
fun setWindSpeed(tv: TextView, weather: LocationWeatherDto?) {
    weather?.let {
        val context = tv.context!!
        tv.text = when(it.unitSystem) {
            UnitSystemDto.IMPERIAL -> context.getString(R.string.wind_speed_imperial,it.windSpeed.toInt())
            UnitSystemDto.METRIC -> context.getString(R.string.wind_speed_metric,it.windSpeed.toInt())
        }
    }
}

@BindingAdapter("setTime")
fun setTime(tv: TextView, time: Long?) {
    time?.let {
        val context = tv.context!!
        val format = SimpleDateFormat(context.getString(R.string.time_format_24))
        tv.text = format.format(Date(it))
    }
}

@BindingAdapter("setUvIndex")
fun setUvIndex(tv: TextView, uvIndex: UvIndexDto?) {
    uvIndex?.let {
        val context = tv.context!!
        tv.text = when(it) {
            UvIndexDto.LOW -> context.getString(R.string.uv_index_low)
            UvIndexDto.MODERATE -> context.getString(R.string.uv_index_moderate)
            UvIndexDto.HIGH -> context.getString(R.string.uv_index_high)
            UvIndexDto.VERY_HIGH -> context.getString(R.string.uv_index_very_high)
        }
    }
}

@BindingAdapter("setMinMaxTemp")
fun setMinMaxTemp(tv: TextView, weather: LocationWeatherDto?) {
    weather?.let {
        val context = tv.context!!
        tv.text = context.getString(R.string.min_max_temp,it.minTemp.toInt(),it.maxTemp.toInt())
    }
}

@BindingAdapter("setLocationIndicator")
fun setLocationIndicator(iv: ImageView,isCurrent: Boolean?) {
    isCurrent?.let {
        when(it) {
            true -> ImageLoader.loadIconResIntoImageView(iv,R.drawable.ic_baseline_location_24)
        }
    }
}