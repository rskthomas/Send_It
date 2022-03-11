package com.buggysoft.sendit;

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat



private const val STROKE_WIDTH = 10f

class DrawingCanvas(context: Context, attrs: AttributeSet? =null) : View(context) {

    //distance that will trigger a correct draw
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    // latest x and y values
    private var currentX = 0f
    private var currentY = 0f

    //x and y coordinates of current touch event
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private lateinit var frame: Rect


    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private var path = Path()

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }


    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        //padding for frame
        val inset = 35
        frame = Rect(inset, inset, width - inset, height - inset)

    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawRect(frame, paint)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }


    //interpolates path for better performance rather than drawing every pixel
    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            canvas.drawPath(path, paint)
        }
        invalidate()
    }



    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
    }



}
