package com.y3owk1n.neru.android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class GridOverlayView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(110, 130, 214)
        strokeWidth = density
        style = Paint.Style.STROKE
    }
    private val activeLinePaint = Paint(linePaint).apply {
        color = Color.WHITE
        strokeWidth = 3f * density
    }
    private val fadedLinePaint = Paint(linePaint).apply {
        color = Color.argb(80, 110, 130, 214)
    }
    private val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(72, 70, 95, 188)
        style = Paint.Style.FILL
    }
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 10f * resources.displayMetrics.scaledDensity
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val subgridTextPaint = Paint(textPaint).apply {
        textSize = 22f * resources.displayMetrics.scaledDensity
    }

    lateinit var navigator: GridNavigator

    fun refresh() = invalidate()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!::navigator.isInitialized) return

        when (navigator.stage) {
            GridNavigator.Stage.MAIN_GRID -> drawMainGrid(canvas)
            GridNavigator.Stage.SUBGRID -> drawSubgrid(canvas)
            GridNavigator.Stage.READY -> drawReadyTarget(canvas)
        }
    }

    private fun drawMainGrid(canvas: Canvas) {
        val prefix = navigator.input.firstOrNull()
        for (row in GridNavigator.PRIMARY_KEYS.indices) {
            for (column in GridNavigator.PRIMARY_KEYS.indices) {
                val bounds = navigator.mainCell(row, column)
                val matches = prefix == null || GridNavigator.PRIMARY_KEYS[row] == prefix
                if (!matches) canvas.drawRect(bounds, dimPaint)
                canvas.drawRect(bounds, if (matches) linePaint else fadedLinePaint)
                drawCenteredText(
                    canvas,
                    "${GridNavigator.PRIMARY_KEYS[row]}${GridNavigator.PRIMARY_KEYS[column]}",
                    bounds,
                    textPaint,
                )
            }
        }
    }

    private fun drawSubgrid(canvas: Canvas) {
        val main = navigator.mainCellBounds ?: return
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        canvas.drawRect(main, shadePaint)
        canvas.drawRect(main, activeLinePaint)

        GridNavigator.SUBGRID_KEYS.forEachIndexed { index, key ->
            val bounds = navigator.subCell(index)
            canvas.drawRect(bounds, activeLinePaint)
            drawCenteredText(canvas, key.toString(), bounds, subgridTextPaint)
        }
    }

    private fun drawReadyTarget(canvas: Canvas) {
        val target = navigator.targetBounds ?: return
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        canvas.drawRect(target, shadePaint)
        canvas.drawRect(target, activeLinePaint)
        drawCenteredText(canvas, "Return", target, textPaint)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, bounds: RectF, paint: Paint) {
        val baseline = bounds.centerY() - (paint.ascent() + paint.descent()) / 2f
        canvas.drawText(text, bounds.centerX(), baseline, paint)
    }
}
