package dev.develsinthedetails.eatpoopyoucat.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.data.Coordinates
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import java.lang.Float.min


// todo move this whole thang to the view model

// todo figure out how to erase
// Stroke width for the the paint.
private const val STROKE_WIDTH = 12f

@SuppressLint("ViewConstructor")
class DrawView(
    context: Context,
    attributeSet: AttributeSet?,
    var drawingPaths: MutableList<Path> = mutableListOf(),
    var undonePaths: MutableList<Path> = mutableListOf(),
    var lineSegments: MutableList<LineSegment> = mutableListOf(),
    var drawingLines: MutableList<Line> = mutableListOf(),
    var undoneLines: MutableList<Line> = mutableListOf(),
    var isReadOnly: Boolean = false,
    var justCleared: Boolean = false,
    val originalResolution: Resolution? = null,
) :
    View(context, attributeSet) {

    private var path: Path = Path()
    private var _strokeWidth = STROKE_WIDTH
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap

    // Set up the paint with which to draw.
    private fun paint() = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = _strokeWidth
    }

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private val scaleMatrix = Matrix()
    private val rectF = RectF()

    /**
     * Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::bitmap.isInitialized) bitmap.recycle()
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        path.computeBounds(rectF, true)
        if(originalResolution != null && originalResolution.height !=0&& originalResolution.width !=0) {
            val xScale: Float = width/ originalResolution.width.toFloat()
            val yScale: Float =  height/ originalResolution.height.toFloat()
            _strokeWidth *= min(xScale,yScale)
            scaleMatrix.setScale(xScale, yScale, rectF.centerX(), rectF.centerY())
        }
        for (p in drawingPaths) {
            p.transform(scaleMatrix)
            canvas.drawPath(p, paint())
        }
        canvas.drawPath(path, paint())
    }

     /**
     * No need to call and implement MyCanvasView#performClick, because MyCanvasView custom view
     * does not handle click actions.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isReadOnly)
            return true

        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    /**
     * The following methods factor out what happens for different touch events,
     * as determined by the onTouchEvent() when statement.
     * This keeps the when conditional block
     * concise and makes it easier to change what happens for each event.
     * No need to call invalidate because we are not drawing anything.
     */
    private fun touchStart() {
        undonePaths.clear()
        undoneLines.clear()
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        LineSegment(Coordinates(currentX, currentY),Coordinates(currentX, currentY))
    }

    private fun touchMove() {
        currentX = min(currentX, canvas.height.toFloat())
        currentY = min(currentY, canvas.width.toFloat())
        motionTouchEventX= min(motionTouchEventX, canvas.height.toFloat())
        motionTouchEventY= min(motionTouchEventY, canvas.width.toFloat())
        path.quadTo(
            currentX,
            currentY,
            (motionTouchEventX + currentX) / 2,
            (motionTouchEventY + currentY) / 2
        )
        lineSegments.add(LineSegment(Coordinates(currentX, currentY),Coordinates(motionTouchEventX, motionTouchEventY)))
        currentX = motionTouchEventX
        currentY = motionTouchEventY

        canvas.drawPath(path, paint())
        invalidate()
    }

    private fun touchUp() {
        currentX = min(currentX,  canvas.height.toFloat())
        currentY = min(currentY, canvas.width.toFloat())
        motionTouchEventX= min(motionTouchEventX, canvas.height.toFloat())
        motionTouchEventY= min(motionTouchEventY, canvas.width.toFloat())
        // draw a dot if no movement but touched.
        if (currentX == motionTouchEventX && currentY == motionTouchEventY) {
            path.lineTo(currentX, currentY)
            canvas.drawPath(path, paint())
        }
        lineSegments.add(LineSegment(Coordinates(currentX, currentY),Coordinates(motionTouchEventX, motionTouchEventY)))
        drawingLines.add(Line(lineSegments))
        lineSegments = mutableListOf()
        drawingPaths.add(path)
        path = Path()
        invalidate()
    }

    fun getDrawing(): Drawing {
        return Drawing(drawingLines)
    }

    fun setErase() {
    }

    fun clearCanvas() {
        undoneLines.clear()
        undoneLines.addAll(drawingLines)
        undonePaths.clear()
        undonePaths.addAll(drawingPaths)
        justCleared= true
        canvas.drawColor(backgroundColor)
        path.reset()
        drawingLines.clear()
        drawingPaths.clear()
        invalidate()
    }

    fun setPen() {
    }

    fun undo() {
        if(justCleared){
            justCleared=false
            drawingPaths.addAll(undonePaths)
            drawingLines.addAll(undoneLines)
            invalidate()
        }
        else if (drawingPaths.size > 0) {
            undonePaths.add(drawingPaths.removeAt(drawingPaths.size - 1))
            undoneLines.add(drawingLines.removeAt(drawingLines.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.size > 0) {
            drawingPaths.add(undonePaths.removeAt(undonePaths.size - 1))
            drawingLines.add(undoneLines.removeAt(undoneLines.size - 1))
            invalidate()
        }
    }
}
