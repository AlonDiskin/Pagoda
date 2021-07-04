package com.diskin.alon.pagoda.weatherinfo.presentation.util

import android.widget.ImageView
import android.widget.TextClock
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.weatherinfo.presentation.R

@BindingAdapter("setImageRes")
fun setImageRes(imageView: ImageView,@DrawableRes res: Int?) {
    res?.let { ImageLoader.loadIconResIntoImageView(imageView, res) }
}

@BindingAdapter("setLocationIndicator")
fun setLocationIndicator(iv: ImageView,isCurrent: Boolean?) {
    isCurrent?.let {
        when(it) {
            true -> ImageLoader.loadIconResIntoImageView(iv,R.drawable.ic_baseline_location_24)
        }
    }
}

@BindingAdapter("setClockTimeZone")
fun setClockTimeZone(tc: TextClock, timeZone: String?) {
    timeZone?.let { tc.timeZone = it }
}