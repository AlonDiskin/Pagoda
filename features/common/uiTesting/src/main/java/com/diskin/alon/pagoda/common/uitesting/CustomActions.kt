package com.diskin.alon.pagoda.common.uitesting

import android.view.View
import android.view.animation.Animation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import io.mockk.mockk
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description
import org.hamcrest.Matcher

fun swipeToRefresh(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return object : BaseMatcher<View>() {
                override fun matches(item: Any): Boolean {
                    return isA(SwipeRefreshLayout::class.java).matches(item)
                }
                override fun describeMismatch(item: Any, mismatchDescription: Description) {
                    mismatchDescription.appendText(
                        "Expected SwipeRefreshLayout or its Descendant, but got other View"
                    )
                }
                override fun describeTo(description: Description) {
                    description.appendText(
                        "Action SwipeToRefresh to view SwipeRefreshLayout or its descendant"
                    )
                }
            }
        }

        override fun getDescription(): String {
            return "Perform swipeToRefresh on the SwipeRefreshLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            val swipeRefreshLayout = view as SwipeRefreshLayout
            swipeRefreshLayout.run {
                isRefreshing = true
                // set mNotify to true
                val fieldNotify = SwipeRefreshLayout::class.java.getDeclaredField("mNotify")
                fieldNotify.isAccessible = true
                fieldNotify.setBoolean(this,true)

                // mockk mRefreshListener onAnimationEnd
                val fieldRefreshListener = SwipeRefreshLayout::class.java.getDeclaredField("mRefreshListener")
                fieldRefreshListener.isAccessible = true
                val animatorListener = fieldRefreshListener.get(this) as Animation.AnimationListener

                animatorListener.onAnimationEnd(mockk())
            }
        }
    }
}
