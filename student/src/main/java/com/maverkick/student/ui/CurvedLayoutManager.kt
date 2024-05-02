package com.maverkick.student.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Integer.min
import kotlin.math.max

/**
 * Layout Manager, which creates the zig-zag like view
 * to display lessons in the LessonAdapter
 **/
class CurvedLayoutManager(context: Context) : LinearLayoutManager(context) {
    companion object {
        const val HORIZONTAL_OFFSET = 250// The maximum horizontal offset for each item
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        detachAndScrapAttachedViews(recycler!!)

        val firstVisiblePosition = max(0, findFirstVisibleItemPosition())
        val lastVisiblePosition = min(itemCount - 1, firstVisiblePosition + 3)

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            val x = calculateXPosition(i, width)
            val y = calculateYPosition(i, height)

            layoutDecorated(view, x, y, x + width, y + height)
        }
    }

    private fun calculateXPosition(position: Int, width: Int): Int {
        val offset = if (position % 2 == 1) HORIZONTAL_OFFSET else -HORIZONTAL_OFFSET
        return (width) / 16 + offset
    }

    private fun calculateYPosition(position: Int, height: Int): Int {
        return height * position + (position * position * 10)
    }

}
