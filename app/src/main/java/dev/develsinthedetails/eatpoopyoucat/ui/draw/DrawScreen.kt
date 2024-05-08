package dev.develsinthedetails.eatpoopyoucat.ui.draw

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
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
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.ErrorText
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.SubmitButton
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.ui.theme.drawingBackground
import dev.develsinthedetails.eatpoopyoucat.ui.theme.drawingPen
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
        linesState = linesState.value,
        currentLineState = currentLineState.value,
        currentPropertiesState = currentPropertiesState.value,
        setCanvasResolution = setCanvasResolution,
        isLoading = drawViewModel.isLoading,
        touchStart = touchStart,
        touchMove = touchMove,
        touchEnd = touchEnd,
        isError = drawViewModel.isError,
        sentence = previousEntry?.sentence,
        onEndedGame = onEndedGame,
        undoCount = undoCount.value,
        redoCount = redoCount.value,
        drawMode = drawViewModel.drawMode,
        setPencilMode = setPencilMode,
        onUndo = undo,
        onRedo = redo,
        onSubmit = onSubmit,
    )
}

@Composable
private fun DrawScreen(
    linesState: List<Line>,
    currentLineState: List<LineSegment>,
    currentPropertiesState: LineProperties = LineProperties(),
    setCanvasResolution: (IntSize) -> Unit,
    isLoading: Boolean = true,
    touchStart: (PointerInputChange) -> Unit,
    touchMove: (PointerInputChange) -> Unit,
    touchEnd: (PointerInputChange) -> Unit,
    isError: Boolean,
    sentence: String?,
    onEndedGame: () -> Unit,
    undoCount: Int,
    redoCount: Int,
    drawMode: DrawMode,
    setPencilMode: (DrawMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffolds.InGame(
        title = stringResource(R.string.draw_turn_title),
        onEnd = onEndedGame,
        bottomBar = {
            BottomAppBar(actions =
            {
                DrawingPropertiesMenu(
                    undoCount = undoCount,
                    redoCount = redoCount,
                    drawMode = drawMode,
                    setPencilMode = setPencilMode,
                    onUndo = onUndo,
                    onRedo = onRedo
                )
            },
                floatingActionButton = {
                    BoxWithConstraints {
                        if (maxWidth < 400.dp)
                            FloatingActionButton(
                                onClick = onSubmit,
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Localized description")
                            }
                        else
                            SubmitButton(onSubmit = onSubmit)
                    }
                }
            )
        }
    )
    { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (isLoading)
                Spinner()
            else
                Box {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        Sentence(sentence)
                        ErrorText(
                            isError,
                            stringResource(id = R.string.drawing_error)
                        )
                        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
}

@Composable
private fun Draw(
    modifier: Modifier,
    linesState: List<Line>,
    currentLineState: List<LineSegment>,
    currentPropertiesState: LineProperties = LineProperties(),
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
            drawingLines = linesState,
            currentLine = currentLineState,
            currentProperties = currentPropertiesState,
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

    val drawingBackground = MaterialTheme.colorScheme.drawingBackground
    val drawingPen = MaterialTheme.colorScheme.drawingPen

    var height = 0
    var width = 0
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .shadow(4.dp)
            .background(drawingBackground)
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
            },
        contentDescription = stringResource(R.string.user_made_drawing)
    )
    {
        val currentResolution = Resolution(height = height, width = width)
        lines.forEach {
            val drawColor = if (it.properties.eraseMode) drawingBackground else drawingPen
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
                    color = drawColor,
                    radius = scaledStroke / 4,
                    center = Offset(bounds.left, bounds.top),
                    style = Stroke(
                        width = scaledStroke / 2
                    ),
                    blendMode = it.properties.blendMode()
                )
            } else
                drawPath(
                    color = drawColor,
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
            Icons.Rounded.Draw,
            tint = selectedTint(drawMode, DrawMode.Draw),
            contentDescription = stringResource(id = R.string.draw),
            modifier = Modifier.size(48.dp),
        )
    }
    IconButton(
        onClick = {
            setPencilMode(DrawMode.Erase)
        },
        modifier = Modifier.background(color = selectedBackground(drawMode, DrawMode.Erase))
    ) {
        Icon(
//            Icons.Rounded.InkEraser,
//          Future: replace with "Icons.Rounded.InkEraser" when it's added to the stable Icons
            painter = painterResource(id = R.drawable.ic_eraser_black_24),
            tint = selectedTint(drawMode, DrawMode.Erase),
            contentDescription = stringResource(id = R.string.erase),
            modifier = Modifier.size(48.dp),
        )
    }
    IconButton(
        onClick = {
            onUndo()
        },
        enabled = (undoCount > 0),
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.Undo,
            contentDescription = stringResource(id = R.string.undo),
            modifier = Modifier.size(48.dp),
        )
    }
    IconButton(
        onClick = {
            onRedo()
        },
        enabled = (redoCount > 0)
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.Redo,
            contentDescription = stringResource(id = R.string.redo),
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun selectedTint(selected: DrawMode, drawMode: DrawMode) =
    if (drawMode == selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun selectedBackground(selected: DrawMode, drawMode: DrawMode) =
    if (drawMode == selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(
        3.dp
    )


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
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = "spec:parent=pixel_fold")
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

    val setCanvasResolution: (IntSize) -> Unit = {}

    val sentence =
//        "Id vero libris usu, vim sale mucius inciderint ut, idque debitis mei ea. Nostrud sapientem patrioque ei mei, et vim nostrum antiopam persequeris, nec everti diceret legendos ea. Te mei adhuc denique, illum evertitur no vim. Vel omnis aliquip ponderum te, per cu modo suas ipsum. Legere vocibus ex vix."
        stringResource(id = R.string.a_cat_winks_at_you_with_the_grace_of_a_very_sleepy_toddler)
    AppTheme {
        DrawScreen(
            linesState = lines,
            currentLineState = listOf(),
            currentPropertiesState = LineProperties(),
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
            undoCount = 1,
            redoCount = 0,
            drawMode = DrawMode.Draw,
            setPencilMode = {},
        )
    }
}

