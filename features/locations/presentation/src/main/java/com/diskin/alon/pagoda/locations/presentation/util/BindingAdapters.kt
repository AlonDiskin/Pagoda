package com.diskin.alon.pagoda.locations.presentation.util

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setLocationCountry")
fun setLocationCountry(tv: TextView, country: String?) {
    country?.let {
        tv.text = when {
            it.isEmpty() -> "Unknown"
            else -> it
        }
    }
}

@BindingAdapter("setLocationState")
fun setLocationState(tv: TextView, state: String?) {
    state?.let {
        if (it.isNotEmpty()) {
            tv.text = ", ".plus(state)
        }
    }
}