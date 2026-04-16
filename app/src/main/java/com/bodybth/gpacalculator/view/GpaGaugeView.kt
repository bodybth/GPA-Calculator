package com.bodybth.gpacalculator.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GpaGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gpa = 0.0

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E8EAF6")
        strokeCap = Paint.Cap.ROUND
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val gpaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D47A1")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        textAlign = Paint.Align.CENTER
    }

    private val minMaxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BDBDBD")
        textAlign = Paint.Align.CENTER
    }

    fun setGpa(value: Double) {
        gpa = value.coerceIn(0.0, 4.0)
        arcPaint.color = when {
            gpa >= 3.5 -> Color.parseColor("#4CAF50")
            gpa >= 3.0 -> Color.parseColor("#F5A623")
            gpa >= 2.0 -> Color.parseColor("#FF9800")
            gpa > 0.0  -> Color.parseColor("#EF5350")
            else       -> Color.parseColor("#F5A623")
        }
        invalidate()
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val strokeW = dp(11f)
        trackPaint.strokeWidth = strokeW
        arcPaint.strokeWidth = strokeW

        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f - strokeW - dp(4f)

        gpaPaint.textSize = r * 0.42f
        labelPaint.textSize = r * 0.175f
        minMaxPaint.textSize = r * 0.155f

        val rect = RectF(cx - r, cy - r, cx + r, cy + r)

        // Background track (150° → 240° sweep)
        canvas.drawArc(rect, 150f, 240f, false, trackPaint)

        // Progress arc
        val sweep = (gpa / 4.0 * 240.0).toFloat()
        if (sweep > 0f) canvas.drawArc(rect, 150f, sweep, false, arcPaint)

        // GPA number centered vertically
        val gpaY = cy + gpaPaint.textSize * 0.33f
        canvas.drawText(String.format("%.2f", gpa), cx, gpaY, gpaPaint)

        // "Cumulative GPA" label
        canvas.drawText("Cumulative GPA", cx, gpaY + labelPaint.textSize * 1.5f, labelPaint)

        // Min / Max labels at arc ends
        val minAngle = Math.toRadians(150.0 + 8.0)
        val maxAngle = Math.toRadians(30.0 - 8.0)
        val lr = r + strokeW * 0.6f
        canvas.drawText(
            "0.0",
            (cx + lr * cos(minAngle)).toFloat(),
            (cy + lr * sin(minAngle)).toFloat() + minMaxPaint.textSize,
            minMaxPaint
        )
        canvas.drawText(
            "4.0",
            (cx + lr * cos(maxAngle)).toFloat(),
            (cy + lr * sin(maxAngle)).toFloat() + minMaxPaint.textSize,
            minMaxPaint
        )
    }
}
