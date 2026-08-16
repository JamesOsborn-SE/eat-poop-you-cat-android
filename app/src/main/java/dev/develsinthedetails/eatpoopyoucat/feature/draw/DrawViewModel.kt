package dev.develsinthedetails.eatpoopyoucat.feature.draw

import android.graphics.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Draw
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.DrawMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Coordinates
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Line
import dev.develsinthedetails.eatpoopyoucat.data.models.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.models.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.models.Resolution
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

enum class DrawMode {
    Draw, Erase
}

class DrawViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {

    var drawMode: DrawMode by mutableStateOf(DrawMode.Draw)
    private var currentX = 0f
    private var currentY = 0f

    private var currentResolution: Resolution = Resolution(0, 0)

    private var lineProperties = MutableStateFlow(LineProperties())
    val lineProps = lineProperties.asLiveData()

    val playerId = appSettings.playerId
    var isError: Boolean by mutableStateOf(false)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Draw>(typeMap)
    private val previousEntryId: Uuid = checkNotNull(route.previousEntryId)
    private val nickname = route.nickname
    private val prevEntry = repository.getEntry(previousEntryId)
    val previousEntry: LiveData<Entry?> = prevEntry.asLiveData()

    val entryId = Uuid.random()

    var drawingLines = MutableStateFlow(listOf<Line>())
        private set
    private var undoneLines = MutableStateFlow(listOf<Line>())

    private var lineSegments: MutableStateFlow<List<LineSegment>> = MutableStateFlow(listOf())
    val lineSeg = lineSegments.asLiveData()
    private var justCleared: Boolean = false

    init {
        clearCanvas()
    }
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

    private fun clearCanvas() {
        undoneLines.value = listOf()
        undoneLines.value += drawingLines.value
        justCleared = true
        drawingLines.value = listOf()
    }

    fun isValidDrawing(onNavigateToSentence: () -> Unit): Boolean {
        if (drawingLines.value.count() < 3
            || currentResolution.height == 0
            || currentResolution.width == 0
        ) {
            isError = true
            return false
        }
        isLoading = true
        viewModelScope.launch {
            val newEntry: Entry = previousEntry.value!!.copy(
                id = entryId,
                localPlayerName = nickname,
                sentence = null,
                drawing = Gzip.compress(Json.encodeToString(drawingLines.value)),
                sequence = previousEntry.value!!.sequence.inc(),
                playerId = playerId
            )
            repository.createEntry(newEntry)
            onNavigateToSentence.invoke()
            isLoading = false
        }
        return true
    }

    fun touchStart(inputChange: PointerInputChange) {
        isError = false
        undoneLines.value = listOf()
        lineSegments.value = listOf()

        currentX = inputChange.position.x
        currentY = inputChange.position.y
        lineSegments.value += (LineSegment(
            Coordinates(currentX, currentY),
            Coordinates(currentX, currentY)
        ))
        inputChange.consume()
    }

    fun touchMove(inputChange: PointerInputChange) {
        currentX = normalizeLocationX(currentX)
        currentY = normalizeLocationY(currentY)
        val motionTouchEventX = normalizeLocationX(inputChange.position.x)
        val motionTouchEventY = normalizeLocationY(inputChange.position.y)
        lineSegments.value += (
                LineSegment(
                    Coordinates(currentX, currentY),
                    Coordinates(motionTouchEventX, motionTouchEventY)
                )
                )
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        inputChange.consume()
    }

    private fun normalizeLocation(x: Float, canvasSize: Int): Float {
        return max(
            0f + lineProperties.value.strokeWidth / 2,
            min(canvasSize.toFloat() - lineProperties.value.strokeWidth / 2, x)
        )
    }

    private fun normalizeLocationX(x: Float): Float {
        return normalizeLocation(x, currentResolution.height)
    }

    private fun normalizeLocationY(y: Float): Float {
        return normalizeLocation(y, currentResolution.width)
    }

    fun touchUp(inputChange: PointerInputChange) {
        currentX = normalizeLocationX(currentX)
        currentY = normalizeLocationY(currentY)
        val motionTouchEventX = normalizeLocationX(inputChange.position.x)
        val motionTouchEventY = normalizeLocationY(inputChange.position.y)
        lineSegments.value += (
                LineSegment(
                    Coordinates(currentX, currentY),
                    Coordinates(motionTouchEventX, motionTouchEventY)
                )
                )
        val daLineSegment = lineSegments.value.toList()
        val daLineProperties = lineProperties.value.copy()
        drawingLines.value += Line(
            daLineSegment,
            daLineProperties,
            Resolution(height = currentResolution.height, width = currentResolution.width)
        )
        lineSegments.value = listOf()
        undoneLines.value = listOf()
        inputChange.consume()
    }

    fun undo() = moveLastToOtherList(fromList = drawingLines, toList = undoneLines)

    fun redo() = moveLastToOtherList(fromList = undoneLines, toList = drawingLines)

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

    fun setCanvasResolution(height: Int, width: Int) {
        currentResolution = Resolution(height, width)
    }

    fun setPencilMode(mode: DrawMode) {
        lineProperties.value.eraseMode = mode == DrawMode.Erase
        drawMode = mode
        if (mode == DrawMode.Erase)
            lineProperties.value.strokeWidth = 48f
        else
            lineProperties.value.strokeWidth = 12f
    }

    fun getGameMode(gameId: Uuid?): GameMode {
        var gameMode = GameMode.LOCAL
        if (gameId != null)
        viewModelScope.launch {
            gameMode = repository.getGame(gameId).gameMode
        }
        return gameMode
    }

    companion object {
        fun scalePath(
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

        fun scaleStroke(
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
