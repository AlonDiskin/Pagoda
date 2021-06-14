package com.diskin.alon.pagoda.common.presentation

import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

object ImageLoader {

    fun loadIconResIntoImageView(imageView: ImageView, @DrawableRes res: Int) {
        imageView.setImageResource(res)
    }

    fun loadIconResIntoImageButton(imageButton: ImageButton, @DrawableRes res: Int) {
        imageButton.setImageResource(res)
    }
}