package com.github.meaninglesscode.todolistapp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Class extending [SwipeRefreshLayout] to support descendant scrolling views that are non-direct.
 * When the scroll view is a direct child, the refresh is only triggered when the [View] is on top.
 * This class adds a way for setScrollUpChild to define which view controls this behavior.
 *
 * @param [context] [Context] context to pass into [SwipeRefreshLayout]
 * @param [attrs] [AttributeSet]? nullable attributes to pass into [SwipeRefreshLayout]
 */
class ScrollChildSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    var scrollUpChild: View? = null

    override fun canChildScrollUp() =
        scrollUpChild?.canScrollVertically(-1) ?: super.canChildScrollUp()
}