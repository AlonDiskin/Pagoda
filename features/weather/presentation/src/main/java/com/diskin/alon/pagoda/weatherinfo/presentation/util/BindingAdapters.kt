package com.diskin.alon.pagoda.weatherinfo.presentation.util

import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextClock
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocationSearchResult
import java.lang.IllegalArgumentException

@BindingAdapter("setImageRes")
fun setImageRes(imageView: ImageView,@DrawableRes res: Int?) {
    res?.let { ImageLoader.loadIconResIntoImageView(imageView, res) }
}

@BindingAdapter("setAddButton")
fun setAddButton(button: ImageButton, location: UiLocationSearchResult?) {
    location?.let {
        when(it.bookmarked) {
            true -> {
                ImageLoader.loadIconResIntoImageButton(button, R.drawable.ic_baseline_done_24)
                button.isEnabled = false
            }
            false -> {
                ImageLoader.loadIconResIntoImageButton(button, R.drawable.ic_baseline_add_24)
                button.isEnabled = true
            }
        }
    }
}

@BindingAdapter("setMainWeatherIconRes")
fun setMainWeatherIconRes(imageView: ImageView,@DrawableRes res: Int?) {
    res?.let {
        val tag: Any? = imageView.tag

        when {
            tag == null -> {
                ImageLoader.loadIconResIntoImageView(imageView, res)
                imageView.tag = res

                when(val drawable = imageView.drawable) {
                    is AnimatedVectorDrawableCompat -> drawable.start()
                    is AnimatedVectorDrawable -> drawable.start()
                }
            }
            tag is Int -> {
                if(tag != res) {
                    ImageLoader.loadIconResIntoImageView(imageView, res)
                    imageView.tag = res

                    when(val drawable = imageView.drawable) {
                        is AnimatedVectorDrawableCompat -> drawable.start()
                        is AnimatedVectorDrawable -> drawable.start()
                    }
                }
            }

            else -> throw IllegalArgumentException("Main weather icon image view tag should be null or int")
        }
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

@BindingAdapter("setClockTimeZone")
fun setClockTimeZone(tc: TextClock, timeZone: String?) {
    timeZone?.let { tc.timeZone = it }
}