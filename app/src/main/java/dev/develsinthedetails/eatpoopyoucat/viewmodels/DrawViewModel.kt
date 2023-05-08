package dev.develsinthedetails.eatpoopyoucat.viewmodels

import android.graphics.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Coordinates
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class DrawViewModel @Inject constructor(
    state: SavedStateHandle,
    private val repository: AppRepository,
) : ViewModel() {
    private var currentX = 0f
    private var currentY = 0f

    var canvasHeight: Int = 0
    var canvasWidth: Int = 0

    private val playerId = SharedPref.playerId()
    var isError: Boolean by mutableStateOf(false)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    private val previousEntryId: String = checkNotNull(state.get<String>("EntryId"))
    val previousEntry: LiveData<Entry> = repository.getEntry(previousEntryId).asLiveData()

    val entryId = UUID.randomUUID().toString()


    private var currentPath: Path = Path()
    var drawingPaths: MutableList<Path> = mutableListOf(Path())
    private var drawingLines: MutableList<Line> = mutableListOf()
    private var undonePaths: MutableList<Path> = mutableListOf()
    private var lineSegments: MutableList<LineSegment> = mutableListOf()
    private var undoneLines: MutableList<Line> = mutableListOf()
    var isReadOnly: Boolean = false
    private var justCleared: Boolean = false

    init {
        clearCanvas()
        drawingPaths = Drawing(drawingLines).toPaths()
    }

    private fun clearCanvas() {
        undoneLines.clear()
        undoneLines.addAll(drawingLines)
        undonePaths.clear()
        undonePaths.addAll(drawingPaths)
        justCleared = true
        drawingLines.clear()
        drawingPaths.clear()
    }

    fun checkDrawing(onNavigateToSentence: () -> Unit) {
        if (canvasHeight == 0 || canvasWidth == 0)
            isError
        isLoading = true
        val newEntry: Entry = previousEntry.value!!.copy(
            id = UUID.fromString(entryId),
            sentence = null,
            drawing = Gzip.compress(Json.encodeToString(drawingLines)),
            height = canvasHeight,
            width = canvasWidth,
            sequence = previousEntry.value!!.sequence.inc(),
            playerId = playerId
        )
        viewModelScope.launch {
            repository.createEntry(newEntry)
            onNavigateToSentence.invoke()
            isLoading = false
        }
    }

    fun touchStart(inputChange: PointerInputChange) {
        undonePaths.clear()
        undoneLines.clear()
        currentPath.reset()
        currentPath.moveTo(inputChange.position.x, inputChange.position.y)
        currentX = inputChange.position.x
        currentY = inputChange.position.y
        LineSegment(Coordinates(currentX, currentY), Coordinates(currentX, currentY))
        inputChange.consume()
    }

    fun touchMove(inputChange: PointerInputChange) {
        currentX = min(currentX, canvasHeight.toFloat())
        currentY = min(currentY, canvasWidth.toFloat())
        val motionTouchEventX = min(inputChange.position.x, canvasHeight.toFloat())
        val motionTouchEventY = min(inputChange.position.y, canvasWidth.toFloat())
        currentPath.quadraticBezierTo(
            currentX,
            currentY,
            (motionTouchEventX + currentX) / 2,
            (motionTouchEventY + currentY) / 2
        )
        lineSegments.add(
            LineSegment(
                Coordinates(currentX, currentY),
                Coordinates(motionTouchEventX, motionTouchEventY)
            )
        )
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        inputChange.consume()
    }

    fun touchUp(inputChange: PointerInputChange) {
        currentX = min(currentX, canvasHeight.toFloat())
        currentY = min(currentY, canvasWidth.toFloat())
        val motionTouchEventX = min(inputChange.position.x, canvasHeight.toFloat())
        val motionTouchEventY = min(inputChange.position.y, canvasWidth.toFloat())
        // draw a dot if no movement but touched.
        if (currentX == motionTouchEventX && currentY == motionTouchEventY) {
            currentPath.lineTo(currentX, currentY)
        }
        lineSegments.add(
            LineSegment(
                Coordinates(currentX, currentY),
                Coordinates(motionTouchEventX, motionTouchEventY)
            )
        )
        drawingLines.add(Line(lineSegments))
        lineSegments = mutableListOf()
        drawingPaths.add(currentPath)
        currentPath = Path()
        inputChange.consume()
    }

    fun doDraw(
        drawScope: DrawScope,
        hasChanged: Boolean,
        originalResolution: Resolution?
    ) {
        Companion.doDraw(
            drawingPaths = drawingPaths,
            drawScope = drawScope,
            currentPath = currentPath,
            hasChanged = hasChanged,
            currentResolution = Resolution(height = canvasHeight, width = canvasWidth),
            originalResolution = originalResolution
        )
    }

    fun undo() = moveLastToOtherList(fromList = drawingPaths, toList = undonePaths)

    fun redo() = moveLastToOtherList(fromList = undonePaths, toList = drawingPaths)

    private fun moveLastToOtherList(fromList: MutableList<Path>, toList: MutableList<Path>) {
        if (fromList.isNotEmpty()) {
            toList.add(fromList.last())
            fromList.removeLast()
        }
    }

    companion object {
        fun doDraw(
            drawingPaths: MutableList<Path>,
            drawScope: DrawScope,
            currentPath: Path,
            hasChanged: Boolean,
            currentResolution: Resolution,
            originalResolution: Resolution?
        ) {
            val scaledStrokeWidth = scaleStroke(currentResolution, originalResolution)
            drawingPaths.forEach {
                val scaledPath = scalePath(it, currentResolution, originalResolution)
                drawScope.drawPath(
                    color = Color.Black,
                    path = scaledPath,
                    style = Stroke(
                        width = scaledStrokeWidth
                    )
                )
            }
            if (hasChanged)
                drawScope.drawPath(
                    color = Color.Black,
                    path = currentPath,
                    style = Stroke(
                        width = scaledStrokeWidth
                    )
                )
        }

        private fun scalePath(
            it: Path,
            currentResolution: Resolution,
            originalResolution: Resolution?
        ): Path {
            val newPath = Path()
            newPath.addPath(it)
            val scaleMatrix = Matrix()
            if (originalResolution != null && originalResolution.height != 0 && originalResolution.width != 0) {
                val yScale: Float = currentResolution.height / originalResolution.height.toFloat()
                val xScale: Float = currentResolution.width / originalResolution.width.toFloat()
                scaleMatrix.postScale(xScale, yScale)
            }
            newPath.asAndroidPath().transform(scaleMatrix)
            return newPath
        }

        private fun scaleStroke(
            currentResolution: Resolution,
            originalResolution: Resolution?,
            strokeWidth: Float = 12f
        ): Float {
            if (originalResolution == null || originalResolution.height == 0 || originalResolution.width == 0) {
                return strokeWidth
            }
            val yScale: Float = currentResolution.height / originalResolution.height.toFloat()
            val xScale: Float = currentResolution.width / originalResolution.width.toFloat()
            return strokeWidth * min(xScale, yScale)
        }
    }
}
