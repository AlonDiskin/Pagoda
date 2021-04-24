package com.diskin.alon.pagoda.common.uitesting

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingResource
import java.util.*


class DataBindingIdlingResource(
    private val activity: FragmentActivity,
    private val rvTag: String
) : IdlingResource {

    private val idlingCallbacks = mutableListOf<IdlingResource.ResourceCallback>()
    private val id = UUID.randomUUID().toString()
    private var wasNotIdle = false

    override fun getName(): String {
        return "DataBinding $id"
    }

    override fun isIdleNow(): Boolean {
        val idle = !getBindings().any { it.hasPendingBindings() }
        @Suppress("LiftReturnOrAssignment")
        if (idle) {
            if (wasNotIdle) {
                // notify observers to avoid espresso race detector
                idlingCallbacks.forEach { it.onTransitionToIdle() }
            }
            wasNotIdle = false
        } else {
            wasNotIdle = true
            // check next frame
            activity.findViewById<View>(android.R.id.content).postDelayed({
                isIdleNow
            }, 16)
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        idlingCallbacks.add(callback)
    }

    private fun getBindings(): List<ViewDataBinding> {
        val fragments = (activity as? FragmentActivity)
            ?.supportFragmentManager
            ?.fragments

        val bindings =
            fragments?.mapNotNull {
                it.view?.getBinding()
            } ?: emptyList()
        val childrenBindings = fragments?.flatMap { it.childFragmentManager.fragments }
            ?.mapNotNull { it.view?.getBinding() } ?: emptyList()

        // customization
        val recyclerViewBindings = fragments?.mapNotNull { getRecyclerViewBinding(it) }
        val res = mutableListOf<ViewDataBinding>()
        recyclerViewBindings?.map { it.map { binding ->  res.add(binding) } }
        /// end customization

        return bindings + childrenBindings + res
    }

    // customization
    private fun getRecyclerViewBinding(fragment: Fragment): List<ViewDataBinding> {
        val bindings = mutableListOf<ViewDataBinding>()
        val view = fragment.requireView()
        val rv = view.findViewWithTag<RecyclerView>(rvTag)

        rv?.let {
            for (i in 0 until rv.childCount) {
                val child = rv.getChildAt(i)
                val childView = rv.getChildViewHolder(child).itemView
                bindings.add(childView.getBinding()!!)
            }
        }

        return bindings
    }
}

private fun View.getBinding(): ViewDataBinding? = DataBindingUtil.getBinding(this)