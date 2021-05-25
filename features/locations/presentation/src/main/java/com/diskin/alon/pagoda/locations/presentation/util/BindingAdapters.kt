package com.diskin.alon.pagoda.locations.presentation.util

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.locations.presentation.R
import com.diskin.alon.pagoda.locations.presentation.model.UiLocationSearchResult

@BindingAdapter("setAddButton")
fun setAddButton(button: ImageButton, location: UiLocationSearchResult) {
    when(location.bookmarked) {
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