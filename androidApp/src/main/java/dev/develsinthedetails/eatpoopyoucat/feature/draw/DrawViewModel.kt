package dev.develsinthedetails.eatpoopyoucat.feature.draw

import android.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Draw
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.core.utilities.generateNickname
import dev.develsinthedetails.eatpoopyoucat.core.utilities.validateNickname
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Coordinates
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Line
import dev.develsinthedetails.eatpoopyoucat.data.models.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.models.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.models.Resolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

enum class DrawMode {
    Draw, Erase
}

data class DrawUiState(
    val gameId: Uuid,
    val gameMode: GameMode,
    val previousEntry: Entry? = null,
    val undoCount: Int = 0,
    val redoCount: Int = 0,
    val drawingLines: List<Line> = emptyList(),
    val currentLineSegment: List<LineSegment> = emptyList(),
    val currentProperties: LineProperties = LineProperties(),
    val drawMode: DrawMode = DrawMode.Draw,
    val isError: Boolean = false,
    val isLoading: Boolean = true,

    val nickname: String? = null,
    val nicknameError: Int? = null,
    val nicknameIsSatisfied: Boolean = false,
    val previousNicknames: List<String> = listOf(),
)

class DrawViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {

    private var currentX = 0f
    private var currentY = 0f
    private var currentResolution: Resolution = Resolution(0, 0)

    private var undoneLines: List<Line> = emptyList()

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Draw>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    private val gameMode = checkNotNull(route.gameMode)
    private val _uiState = MutableStateFlow(DrawUiState(gameId, gameMode))
    val uiState: StateFlow<DrawUiState> = _uiState.asStateFlow()
    val entryId = Uuid.random()
    val playerId = appSettings.playerId

    init {
        clearCanvas()
        viewModelScope.launch {
            val doNotUseNicknames= !appSettings.useNicknamesFlow.first() && gameMode == GameMode.LOCAL
            val entry = repository.getLastEntry(gameId)
            _uiState.update { it.copy(previousEntry = entry, isLoading = false, nicknameIsSatisfied = doNotUseNicknames) }
        }
    }

    private fun clearCanvas() {
        undoneLines = undoneLines + _uiState.value.drawingLines
        _uiState.update {
            it.copy(
                drawingLines = emptyList(),
                undoCount = 0,
                redoCount = undoneLines.size
            )
        }
    }

    fun isValidDrawing(onNavigateToSentence: () -> Unit): Boolean {
        val currentState = _uiState.value
        if (currentState.drawingLines.size < 3
            || currentResolution.height == 0
            || currentResolution.width == 0
        ) {
            _uiState.update { it.copy(isError = true) }
            return false
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val previousEntry = currentState.previousEntry
            if (previousEntry != null) {
                val newEntry: Entry = previousEntry.copy(
                    id = entryId,
                    localPlayerName = currentState.nickname,
                    sentence = null,
                    drawing = Gzip.compress(Json.encodeToString(currentState.drawingLines)),
                    sequence = previousEntry.sequence.inc(),
                    playerId = playerId
                )
                repository.createEntry(newEntry)
                onNavigateToSentence.invoke()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
        return true
    }

    fun touchStart(inputChange: PointerInputChange) {
        undoneLines = emptyList()

        currentX = inputChange.position.x
        currentY = inputChange.position.y

        _uiState.update { state ->
            state.copy(
                isError = false,
                redoCount = 0, // Redo is cleared when new drawing starts
                currentLineSegment = listOf(
                    LineSegment(Coordinates(currentX, currentY), Coordinates(currentX, currentY))
                )
            )
        }
        inputChange.consume()
    }

    fun touchMove(inputChange: PointerInputChange) {
        currentX = normalizeLocationX(currentX)
        currentY = normalizeLocationY(currentY)
        val motionTouchEventX = normalizeLocationX(inputChange.position.x)
        val motionTouchEventY = normalizeLocationY(inputChange.position.y)

        val newSegment = LineSegment(
            Coordinates(currentX, currentY),
            Coordinates(motionTouchEventX, motionTouchEventY)
        )

        _uiState.update { state ->
            state.copy(
                currentLineSegment = state.currentLineSegment + newSegment
            )
        }

        currentX = motionTouchEventX
        currentY = motionTouchEventY
        inputChange.consume()
    }

    fun touchUp(inputChange: PointerInputChange) {
        currentX = normalizeLocationX(currentX)
        currentY = normalizeLocationY(currentY)
        val motionTouchEventX = normalizeLocationX(inputChange.position.x)
        val motionTouchEventY = normalizeLocationY(inputChange.position.y)

        val newSegment = LineSegment(
            Coordinates(currentX, currentY),
            Coordinates(motionTouchEventX, motionTouchEventY)
        )

        _uiState.update { state ->
            val finalSegments = state.currentLineSegment + newSegment
            val newLine = Line(
                finalSegments,
                state.currentProperties.copy(),
                Resolution(height = currentResolution.height, width = currentResolution.width)
            )
            val updatedLines = state.drawingLines + newLine

            state.copy(
                drawingLines = updatedLines,
                currentLineSegment = emptyList(),
                undoCount = updatedLines.size
            )
        }

        undoneLines = emptyList()
        inputChange.consume()
    }

    private fun normalizeLocation(x: Float, canvasSize: Int): Float {
        val strokeWidth = _uiState.value.currentProperties.strokeWidth
        return max(
            0f + strokeWidth / 2,
            min(canvasSize.toFloat() - strokeWidth / 2, x)
        )
    }

    private fun normalizeLocationX(x: Float): Float {
        return normalizeLocation(x, currentResolution.height)
    }

    private fun normalizeLocationY(y: Float): Float {
        return normalizeLocation(y, currentResolution.width)
    }

    fun undo() {
        val currentLines = _uiState.value.drawingLines
        if (currentLines.isNotEmpty()) {
            val popped = currentLines.last()
            val newLines = currentLines.dropLast(1)
            undoneLines = undoneLines + popped

            _uiState.update {
                it.copy(
                    drawingLines = newLines,
                    undoCount = newLines.size,
                    redoCount = undoneLines.size
                )
            }
        }
    }

    fun redo() {
        if (undoneLines.isNotEmpty()) {
            val popped = undoneLines.last()
            undoneLines = undoneLines.dropLast(1)
            val newLines = _uiState.value.drawingLines + popped

            _uiState.update {
                it.copy(
                    drawingLines = newLines,
                    undoCount = newLines.size,
                    redoCount = undoneLines.size
                )
            }
        }
    }

    fun setCanvasResolution(height: Int, width: Int) {
        currentResolution = Resolution(height, width)
    }

    fun setPencilMode(mode: DrawMode) {
        _uiState.update { state ->
            val newStrokeWidth = if (mode == DrawMode.Erase) 48f else 12f
            state.copy(
                drawMode = mode,
                currentProperties = state.currentProperties.copy(
                    eraseMode = mode == DrawMode.Erase,
                    strokeWidth = newStrokeWidth
                )
            )
        }
    }

    fun getGameMode(gameId: Uuid?): GameMode {
        var gameMode = GameMode.LOCAL
        if (gameId != null) {
            viewModelScope.launch {
                gameMode = repository.getGame(gameId).gameMode
            }
        }
        return gameMode
    }

    fun updateNickname(nickname: String?) {
        _uiState.update { state ->
            state.copy(
                nickname = nickname
            )
        }
    }

    fun isNicknameValid(hardcodedNicknames: List<String>, fallbackNickname: String) {
        viewModelScope.launch {
            val pun = repository.getPreviouslyUsedNicknames(_uiState.value.gameId)
            val isValid = validateNickname(_uiState.value.nickname, pun)
            if (!isValid) {
                val generatedNick = generateNickname(hardcodedNicknames, pun, fallbackNickname)
                _uiState.update {
                    it.copy(
                        nickname = generatedNick,
                        nicknameError = R.string.no_nickname_chosen_warning
                    )
                }
            } else {
                _uiState.update { it.copy(nicknameIsSatisfied = true) }
            }
        }
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