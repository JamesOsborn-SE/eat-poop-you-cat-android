package dev.develsinthedetails.eatpoopyoucat.compose.draw

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Buttons
import dev.develsinthedetails.eatpoopyoucat.compose.ErrorText
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.getFill
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.utilities.catTestDrawingLinesInJson
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawMode
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun DrawScreen(
    viewModel: DrawViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
    onNavigateToEndedGame: (String) -> Unit
) {
    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.background
        ) {
            if (viewModel.isLoading)
                Spinner()
            Column {
                val previousEntry by viewModel.previousEntry.observeAsState()
                Sentence(previousEntry?.sentence)

                Draw(modifier = getFill())
                ErrorText(viewModel.isError, stringResource(id = R.string.drawing_error))

                Buttons(onSubmit = {
                    viewModel.checkDrawing { onNavigateToSentence(viewModel.entryId) }
                }, onEnd = { onNavigateToEndedGame(previousEntry?.gameId.toString()) })
            }
        }
    }
}

@Composable
fun Sentence(sentence: String?) {
    if(sentence.isNullOrBlank())
        return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .border(2.dp, color = MaterialTheme.colorScheme.onTertiary)

    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.draw_this_sentence),
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


@Composable
fun Draw(
    modifier: Modifier = Modifier,
    drawViewModel: DrawViewModel = hiltViewModel(),
    isReadOnly: Boolean = false,
    drawingLines: ArrayList<Line> = ArrayList(),
) {
    drawViewModel.isReadOnly(isReadOnly)

    if (drawingLines.isNotEmpty()) {
        drawViewModel.setLines(drawingLines)
    }

    var hasChanged by remember { mutableStateOf(false) }
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

    Draw(
        getFill(),
        linesState,
        currentLineState,
        currentPropertiesState,
        setCanvasResolution,
        isReadOnly,
        touchStart,
        touchMove,
        touchEnd,
        undoCount,
        redoCount,
        drawViewModel.drawMode,
        setPencilMode,
        undo,
        redo
    )
}

@Composable
private fun Draw(
    modifier: Modifier,
    linesState: State<List<Line>>,
    currentLineState: State<List<LineSegment>>,
    currentPropertiesState: State<LineProperties>,
    setCanvasResolution: (IntSize) -> Unit,
    isReadOnly: Boolean,
    touchStart: (PointerInputChange) -> Unit,
    touchMove: (PointerInputChange) -> Unit,
    touchEnd: (PointerInputChange) -> Unit,
    undoCount: State<Int>,
    redoCount: State<Int>,
    drawMode: DrawMode,
    setPencilMode: (DrawMode) -> Unit,
    undo: () -> Unit,
    redo: () -> Unit
) {
    Box(
        modifier = getFill()
            .aspectRatio(1f)
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
                        if (isReadOnly) {
                            pointerInputChange.consume()

                        } else {
                            touchStart(pointerInputChange)
                        }
                    },
                    onDrag = { pointerInputChange ->
                        if (isReadOnly) {
                            pointerInputChange.consume()

                        } else {
                            touchMove(pointerInputChange)
                        }
                    },
                    onDragEnd = { pointerInputChange ->
                        if (isReadOnly) {
                            pointerInputChange.consume()
                        } else {
                            touchEnd(pointerInputChange)
                        }
                    })
        )
    }


    DrawingPropertiesMenu(
        undoCount = undoCount.value,
        redoCount = redoCount.value,
        drawMode = drawMode,
        setPencilMode = setPencilMode,
        onUndo = undo,
        onRedo = redo
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawBox(
    modifier: Modifier = Modifier,
    drawingZippedJson: ByteArray = byteArrayOf(),
    drawingLines: List<Line> = listOf(),
    currentLine: List<LineSegment> = listOf(),
    currentProperties: LineProperties = LineProperties(),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val lines: MutableList<Line> = if (drawingLines.isNotEmpty())
        drawingLines.toMutableList()
    else if (drawingZippedJson.isNotEmpty())
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
            .padding(all = 8.dp)
            .background(color = Color.White)
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
    modifier: Modifier = Modifier,
    undoCount: Int = 0,
    redoCount: Int = 0,
    drawMode: DrawMode,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    setPencilMode: (DrawMode) -> Unit,
) {
    modifier
        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
        .shadow(1.dp, RoundedCornerShape(8.dp))
        .background(Color.White)
        .padding(4.dp)

    OrientationSwapperEvenly(
        modifier = modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(4.dp)
            .fillMaxHeight(),
        rowModifier = modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(4.dp)
            .fillMaxWidth(),
        flip = true,
        {
            IconButton(onClick = {
                setPencilMode(DrawMode.Draw)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_draw_black_24dp),
                    contentDescription = stringResource(id = R.string.erase),
                    tint = if (drawMode == DrawMode.Draw) Color.Black else Color.LightGray
                )
            }
        },
        {
            IconButton(onClick = {
                setPencilMode(DrawMode.Erase)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eraser_black_24dp),
                    contentDescription = stringResource(id = R.string.erase),
                    tint = if (drawMode == DrawMode.Erase) Color.Black else Color.LightGray
                )
            }
        },
        {
            IconButton(onClick = {
                onUndo()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo_black_24dp),
                    contentDescription = stringResource(id = R.string.undo),
                    tint = if (undoCount > 0) Color.Black else Color.LightGray
                )
            }
        },
        {
            IconButton(onClick = {
                onRedo()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_redo_black_24dp),
                    contentDescription = stringResource(id = R.string.redo),
                    tint = if (redoCount > 0) Color.Black else Color.LightGray
                )
            }
        }
    )
}

@Composable
fun OrientationSwapper(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    flip: Boolean = false,
    vararg contents: @Composable () -> Unit
) {
    val isColumn =
        flip.xor(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
    if (isColumn) {
        Column(
            modifier = modifier
        ) {
            for (content in contents) {
                content()
            }
        }
    } else {
        Row(
            modifier = rowModifier
        ) {
            for (content in contents) {
                content()
            }
        }
    }
}

@Composable
fun OrientationSwapperEvenly(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    flip: Boolean = false,
    vararg contents: @Composable () -> Unit
) {
    val isColumn =
        flip.xor(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
    if (isColumn) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            for (content in contents) {
                content()
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = rowModifier
        ) {
            for (content in contents) {
                content()
            }
        }
    }
}

@Preview
@Composable
fun PreviewDawingWithSentance() {
    val lines = Json.decodeFromString<List<Line>>(catTestDrawingLinesInJson)
    val lineState = remember {
        mutableStateOf(lines)
    }
    val currentLineState = remember {
        mutableStateOf(listOf<LineSegment>())
    }
    val currentPropertiesState = remember {
        mutableStateOf(LineProperties())
    }
    val setCanvasResolution: (IntSize) -> Unit = {}
    val isReadOnly = false
    val touch: (PointerInputChange) -> Unit = {}
    val undoCount = remember {
        mutableStateOf(1)
    }
    val redoCount = remember {
        mutableStateOf(0)
    }

    OrientationSwapper(
        modifier = Modifier,
        rowModifier = Modifier,
        flip = false,
        {
            Draw(
                getFill(),
                linesState = lineState,
                currentLineState,
                currentPropertiesState,
                setCanvasResolution,
                isReadOnly,
                touchStart = touch,
                touchMove = touch,
                touchEnd = touch,
                undoCount,
                redoCount,
                drawMode = DrawMode.Draw,
                setPencilMode = {},
                undo = {}
            ) {}
        },
        {
            Column {
                Sentence(sentence = "a cat winks at you with the grace of a toddler on benedryl")

                Buttons(onSubmit = {}, onEnd = {})
            }
        }
    )
}

@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Composable
fun PreviewDawingWithSentanceLandscape() {
    PreviewDawingWithSentance()
}