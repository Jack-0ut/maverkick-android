package com.maverkick.student.ui

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView

/**
 * Line that connects lessons in the Daily Learning Path
 **/
class PathItemDecoration : RecyclerView.ItemDecoration() {
    private val paint = Paint().apply {
        strokeWidth = 10f
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, Color.parseColor("#FDFCFC"))
    }

    private val gradientColors = intArrayOf(
        Color.parseColor("#EFE1EF"),  // Starting color
        Color.parseColor("#A9D0A9")   // Ending color
    )

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val gradientPaint = LinearGradient(0f, 0f, parent.width.toFloat(), 0f,
            gradientColors, null, Shader.TileMode.CLAMP)
        paint.shader = gradientPaint

        for (i in 0 until parent.childCount - 1) {
            val startView = parent.getChildAt(i)
            val endView = parent.getChildAt(i + 1)

            val startX = parent.layoutManager!!.getDecoratedRight(startView).toFloat() - startView.width / 2
            val startY = parent.layoutManager!!.getDecoratedTop(startView).toFloat() + startView.height / 2

            val endX = parent.layoutManager!!.getDecoratedLeft(endView).toFloat() + endView.width / 2
            val endY = parent.layoutManager!!.getDecoratedTop(endView).toFloat() + endView.height / 2

            c.drawLine(startX, startY, endX, endY, paint)
        }
    }
}
