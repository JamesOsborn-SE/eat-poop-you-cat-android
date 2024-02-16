package dev.develsinthedetails.eatpoopyoucat.compose.draw

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.ConfirmDialog
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.ErrorText
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.SubmitButton
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawMode
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.serialization.json.Json

@Composable
fun DrawScreen(
    drawViewModel: DrawViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
    onNavigateToEndedGame: (String) -> Unit
) {
    val previousEntry by drawViewModel.previousEntry.observeAsState()
    val undoCount = drawViewModel.undoCount.observeAsState(initial = 0)
    val redoCount = drawViewModel.redoCount.observeAsState(initial = 0)
    val linesState = drawViewModel.drawingLines.asLiveData().observeAsState(initial = listOf())
    val currentLineState = drawViewModel.lineSeg.observeAsState(initial = listOf())
    val currentPropertiesState = drawViewModel.lineProps.observeAsState(initial = LineProperties())
    val setCanvasResolution: (IntSize) -> Unit =
        { drawViewModel.setCanvasResolution(it.height, it.width) }
    val touchStart: (PointerInputChange) -> Unit = { drawViewModel.touchStart(it) }
    val touchMove: (PointerInputChange) -> Unit = { drawViewModel.touchMove(it) }
    val touchEnd: (PointerInputChange) -> Unit = { drawViewModel.touchUp(it) }
    val setPencilMode: (DrawMode) -> Unit = { drawViewModel.setPencileMode(it) }
    val undo = { drawViewModel.undo() }
    val redo = { drawViewModel.redo() }
    val context = LocalContext.current
    val toastText = stringResource(id = R.string.pass_to_the_next)

    val onEndedGame =
        { onNavigateToEndedGame(previousEntry?.gameId.toString()) }

    val onSubmit = {
        if (drawViewModel.isValidDrawing { onNavigateToSentence(drawViewModel.entryId) })
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
    }

    DrawScreen(
        linesState = linesState,
        currentLineState = currentLineState,
        currentPropertiesState = currentPropertiesState,
        setCanvasResolution = setCanvasResolution,
        isLoading = drawViewModel.isLoading,
        touchStart = touchStart,
        touchMove = touchMove,
        touchEnd = touchEnd,
        isError = drawViewModel.isError,
        sentence = previousEntry?.sentence,
        onEndedGame = onEndedGame,
        undoCount = undoCount,
        redoCount = redoCount,
        drawMode = drawViewModel.drawMode,
        setPencilMode = setPencilMode,
        onUndo = undo,
        onRedo = redo,
        onSubmit = onSubmit
    )
}

@Composable
private fun DrawScreen(
    linesState: State<List<Line>>,
    currentLineState: State<List<LineSegment>>,
    currentPropertiesState: State<LineProperties>,
    setCanvasResolution: (IntSize) -> Unit,
    isLoading: Boolean = true,
    touchStart: (PointerInputChange) -> Unit,
    touchMove: (PointerInputChange) -> Unit,
    touchEnd: (PointerInputChange) -> Unit,
    isError: Boolean,
    sentence: String?,
    onEndedGame: () -> Unit,
    undoCount: State<Int>,
    redoCount: State<Int>,
    drawMode: DrawMode,
    setPencilMode: (DrawMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSubmit: () -> Unit,

    ) {
    val height = remember { mutableIntStateOf(0) }
    val width = remember { mutableIntStateOf(0) }
    var showEndGameConfirm by remember { mutableStateOf(false) }
    Scaffolds.InGame(
        title = stringResource(R.string.draw_turn_title),
        showEndGameConfirm = { showEndGameConfirm = true },
        bottomBar = {
            BottomAppBar(actions =
            {
                DrawingPropertiesMenu(
                    undoCount = undoCount.value,
                    redoCount = redoCount.value,
                    drawMode = drawMode,
                    setPencilMode = setPencilMode,
                    onUndo = onUndo,
                    onRedo = onRedo
                )
            },
                floatingActionButton = { SubmitButton(onSubmit = onSubmit) }
        )
        }
    )
    { innerPadding ->
        BackHandler(
            enabled = true
        ) {
            showEndGameConfirm = true
        }
        if (showEndGameConfirm) {
            ConfirmDialog(
                onDismiss = { showEndGameConfirm = false },
                onConfirm = onEndedGame,
                action = stringResource(R.string.end_game_for_all)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading)
                Spinner()
            Column(
                modifier = Modifier
                    .onPlaced {
                        height.intValue = it.size.height
                        width.intValue = it.size.width
                    }
                    .onSizeChanged {
                        height.intValue = it.height
                        width.intValue = it.width
                    }) {
                Column(modifier = Modifier) {
                    Sentence(sentence)
                    ErrorText(
                        isError,
                        stringResource(id = R.string.drawing_error)
                    )
                }
                var modifier = Modifier.padding(0.dp)
                modifier =
                    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        val size =
                            (height.intValue.coerceAtMost(width.intValue) / 2).dp - innerPadding.calculateTopPadding() - innerPadding.calculateBottomPadding()
                        modifier.then(Modifier.size(size))
                    } else {
                        modifier.then(Modifier.fillMaxSize())
                    }

                Row(modifier = modifier)
                {
                    Draw(
                        Modifier,
                        linesState,
                        currentLineState,
                        currentPropertiesState,
                        setCanvasResolution,
                        touchStart,
                        touchMove,
                        touchEnd,
                    )
                }
            }
        }
    }
}

@Composable
private fun Draw(
    modifier: Modifier,
    linesState: State<List<Line>>,
    currentLineState: State<List<LineSegment>>,
    currentPropertiesState: State<LineProperties>,
    setCanvasResolution: (IntSize) -> Unit,
    touchStart: (PointerInputChange) -> Unit,
    touchMove: (PointerInputChange) -> Unit,
    touchEnd: (PointerInputChange) -> Unit,
) {
    Box(
        modifier = modifier
            .padding(all = 8.dp)
    ) {

        DrawBox(
            drawingLines = linesState.value,
            currentLine = currentLineState.value,
            currentProperties = currentPropertiesState.value,
            modifier = modifier
        )

        Box(
            modifier = modifier
                .aspectRatio(1f)
                .padding(all = 8.dp)
                .onPlaced {
                    setCanvasResolution(it.size)
                }
                .onSizeChanged {
                    setCanvasResolution(it)
                }
                .dragMotionEvent(
                    onDragStart = { pointerInputChange ->
                        touchStart(pointerInputChange)
                    },
                    onDrag = { pointerInputChange ->
                        touchMove(pointerInputChange)
                    },
                    onDragEnd = { pointerInputChange ->
                        touchEnd(pointerInputChange)
                    })
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawBox(
    modifier: Modifier = Modifier,
    drawingZippedJson: ByteArray? = byteArrayOf(),
    drawingLines: List<Line> = listOf(),
    currentLine: List<LineSegment> = listOf(),
    currentProperties: LineProperties = LineProperties(),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val lines: MutableList<Line> = if (drawingLines.isNotEmpty())
        drawingLines.toMutableList()
    else if (drawingZippedJson!!.isNotEmpty())
        Json.decodeFromString(Gzip.decompressToString(drawingZippedJson))
    else
        mutableListOf()

    if (currentLine.isNotEmpty())
        lines.add(Line(currentLine, currentProperties))

    var height = 0
    var width = 0
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .shadow(4.dp)
            .background(Color.White)
            .border(border = BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground))
            .combinedClickable(
                onLongClick = { onLongClick.invoke() },
                onClick = { onClick.invoke() }
            )
            .onPlaced {
                height = it.size.height
                width = it.size.width
            }
            .onSizeChanged {
                height = it.height
                width = it.width
            })
    {
        val currentResolution = Resolution(height = height, width = width)
        lines.forEach {
            val path = it.toPath()
            val scaledPath = DrawViewModel.scalePath(path, currentResolution, it.resolution)
            val scaledStroke = DrawViewModel.scaleStroke(
                currentResolution,
                it.resolution,
                it.properties.strokeWidth
            )
            val isSinglePoint = it.lineSegments.count() == 2
                    && it.lineSegments[0] == it.lineSegments[1]

            if (isSinglePoint) {
                val bounds = scaledPath.getBounds()
                drawCircle(
                    color = it.properties.drawColor(),
                    radius = scaledStroke / 4,
                    center = Offset(bounds.left, bounds.top),
                    style = Stroke(
                        width = scaledStroke / 2
                    ),
                    blendMode = it.properties.blendMode()
                )
            } else
                drawPath(
                    color = it.properties.drawColor(),
                    path = scaledPath,
                    style = Stroke(
                        width = scaledStroke,
                        cap = StrokeCap.Round
                    ),
                    blendMode = it.properties.blendMode()
                )
        }
    }
}

@Composable
private fun DrawingPropertiesMenu(
    undoCount: Int = 0,
    redoCount: Int = 0,
    drawMode: DrawMode,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    setPencilMode: (DrawMode) -> Unit,
) {
        IconButton(
            onClick = {
                setPencilMode(DrawMode.Draw)
            },
            modifier = Modifier.background(color = selectedBackground(drawMode, DrawMode.Draw))

        ) {
            Icon(
                tint = selectedTint(drawMode, DrawMode.Draw),
                painter = painterResource(id = R.drawable.ic_draw_black_24),
                contentDescription = stringResource(id = R.string.erase),
            )
        }
        IconButton(
            onClick = {
                setPencilMode(DrawMode.Erase)
            },
            modifier = Modifier.background(color = selectedBackground(drawMode, DrawMode.Erase))
        ) {
            Icon(
                tint = selectedTint(drawMode, DrawMode.Erase),
                painter = painterResource(id = R.drawable.ic_eraser_black_24),
                contentDescription = stringResource(id = R.string.erase),
            )
        }
        IconButton(
            onClick = {
                onUndo()
            },
            enabled = (undoCount > 0)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo_black_24),
                contentDescription = stringResource(id = R.string.undo),
            )
        }
        IconButton(
            onClick = {
                onRedo()
            },
            enabled = (redoCount > 0)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_redo_black_24),
                contentDescription = stringResource(id = R.string.redo),
            )
        }
}

@Composable
private fun selectedTint(selected: DrawMode, drawMode: DrawMode) =
    if (drawMode == selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun selectedBackground(selected: DrawMode, drawMode: DrawMode) =
    if (drawMode == selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)


@Composable
fun Sentence(sentence: String?) {
    if (sentence.isNullOrBlank())
        return
    Column(
        modifier = Modifier
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .padding(4.dp)

    ) {
        Text(
            modifier = Modifier.padding(PaddingValues(bottom = 8.dp, start = 8.dp)),
            text = stringResource(R.string.no_letters_or_numbers),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = sentence,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = 24.sp
        )
    }
}

/**
 * Preview Screenshot #3
 */
@Preview
@Preview(device = "spec:parent=Nexus 7 2013")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    device = "spec:parent=Nexus 7 2013,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewDawingWithSentance() {
    val lines =
        Json.decodeFromString<List<Line>>(dev.develsinthedetails.eatpoopyoucat.utilities.catTestDrawingLinesInJson)
    val linesState = remember {
        mutableStateOf(lines)
    }
    val currentLineState = remember {
        mutableStateOf(listOf<LineSegment>())
    }
    val currentPropertiesState = remember {
        mutableStateOf(LineProperties())
    }
    val setCanvasResolution: (IntSize) -> Unit = {}
    val undoCount = remember {
        mutableIntStateOf(1)
    }
    val redoCount = remember {
        mutableIntStateOf(0)
    }
    val sentence = dev.develsinthedetails.eatpoopyoucat.utilities.catSentence
    AppTheme {
        DrawScreen(
            linesState = linesState,
            currentLineState = currentLineState,
            currentPropertiesState = currentPropertiesState,
            setCanvasResolution = setCanvasResolution,
            isLoading = false,
            touchStart = { },
            touchMove = { },
            touchEnd = { },
            isError = false,
            sentence = sentence,
            onEndedGame = {},
            onRedo = {},
            onUndo = {},
            onSubmit = {},
            undoCount = undoCount,
            redoCount = redoCount,
            drawMode = DrawMode.Draw,
            setPencilMode = {}
        )
    }
}

