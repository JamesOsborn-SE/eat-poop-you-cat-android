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
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
        private set
    var canvasWidth: Int = 0
        private set

    private val playerId = SharedPref.playerId()
    var isError: Boolean by mutableStateOf(false)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    private val previousEntryId: String = checkNotNull(state.get<String>("EntryId"))
    val previousEntry: LiveData<Entry> = repository.getEntry(previousEntryId).asLiveData()

    val entryId = UUID.randomUUID().toString()

    private var currentPath: Path = Path()

    private var drawingLines = MutableStateFlow(listOf<Line>())
    private var undoneLines = MutableStateFlow(listOf<Line>())

    private var lineSegments: MutableList<LineSegment> = mutableListOf()
    var isReadOnly: Boolean = false
        private set
    private var justCleared: Boolean = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val undoCount = drawingLines.flatMapLatest { lines ->
        flow {
            emit(lines.count())
        }
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val redoCount = undoneLines.flatMapLatest { lines ->
        flow {
            emit(lines.count())
        }
    }.asLiveData()

    init {
        clearCanvas()
    }

    private fun clearCanvas() {
        undoneLines.value = listOf()
        undoneLines.value += drawingLines.value
        justCleared = true
        drawingLines.value = listOf()
    }

    fun checkDrawing(onNavigateToSentence: () -> Unit) {
        if (canvasHeight == 0 || canvasWidth == 0)
            isError
        isLoading = true
        val newEntry: Entry = previousEntry.value!!.copy(
            id = UUID.fromString(entryId),
            sentence = null,
            drawing = Gzip.compress(Json.encodeToString(drawingLines.value)),
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
        undoneLines.value = listOf()
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
        drawingLines.value += Line(lineSegments)
        lineSegments = mutableListOf()
        undoneLines.value = listOf()
        currentPath = Path()
        inputChange.consume()
    }

    fun doDraw(
        drawScope: DrawScope,
        hasChanged: Boolean,
        originalResolution: Resolution?
    ) {
        Companion.doDraw(
            drawingLines = drawingLines.value.toMutableList(),
            drawScope = drawScope,
            currentPath = currentPath,
            hasChanged = hasChanged,
            currentResolution = Resolution(height = canvasHeight, width = canvasWidth),
            originalResolution = originalResolution
        )
    }

    fun undo() = moveLastToOtherList(fromList = drawingLines, toList = undoneLines)

    fun redo() = moveLastToOtherList(fromList = undoneLines, toList = drawingLines)

    fun setLines(lines: ArrayList<Line>) {
        drawingLines.value = lines
    }

    private fun <T> moveLastToOtherList(
        fromList: MutableStateFlow<List<T>>,
        toList: MutableStateFlow<List<T>>
    ) {
        if (fromList.value.isNotEmpty()) {
            val popped = fromList.value.last()
            fromList.value -= popped
            toList.value += popped
        }
    }

    fun isReadOnly(readOnly: Boolean) {
        isReadOnly = readOnly
    }

    fun canvasHeight(height: Int) {
        canvasHeight = height
    }

    fun canvasWidth(width: Int) {
        canvasWidth = width
    }

    companion object {
        fun doDraw(
            drawingLines: List<Line>,
            drawScope: DrawScope,
            currentPath: Path,
            hasChanged: Boolean,
            currentResolution: Resolution,
            originalResolution: Resolution?
        ) {
            val scaledStrokeWidth = scaleStroke(currentResolution, originalResolution)
            drawingLines.forEach {
                val path = it.toPath()
                val scaledPath = scalePath(path, currentResolution, originalResolution)
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
