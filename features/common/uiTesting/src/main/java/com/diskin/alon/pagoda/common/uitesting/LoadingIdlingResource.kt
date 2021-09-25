package com.diskin.alon.pagoda.common.uitesting

import android.app.Activity
import android.widget.ProgressBar
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.IdlingResource

class LoadingIdlingResource constructor(
    private val activity: Activity,
    @IdRes private val pbId: Int,
    @IdRes private val rbId: Int
) : IdlingResource {

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return "LoadingIdlingResource"
    }

    override fun isIdleNow(): Boolean {
        return if (!isProgressShowing()) {
            resourceCallback?.onTransitionToIdle()
            true
        } else {
            false
        }
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

    private fun isProgressShowing(): Boolean {
        val progress: ProgressBar? = activity.findViewById(pbId)
        val refreshLayout: SwipeRefreshLayout? = activity.findViewById(rbId)

        return refreshLayout?.isRefreshing ?: false || progress?.isVisible ?: false
    }
}