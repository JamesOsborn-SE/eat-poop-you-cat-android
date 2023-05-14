package dev.develsinthedetails.eatpoopyoucat.compose.draw

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Buttons
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.getFill
import dev.develsinthedetails.eatpoopyoucat.compose.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineProperties
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
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
    EatPoopYouCatTheme {
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
                previousEntry?.sentence?.let {
                    Sentence(it)
                }
                Draw(modifier = getFill())

                if (viewModel.isError) {
                    Text(text = stringResource(id = R.string.drawing_error), color = Color.Red)
                }
                Buttons(onSubmit = {
                    viewModel.checkDrawing { onNavigateToSentence(viewModel.entryId) }
                }, onEnd = { onNavigateToEndedGame(previousEntry?.gameId.toString()) })
            }
        }
    }
}

@Composable
fun Sentence(it: String) {
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
        Text(modifier = Modifier.padding(8.dp),
            text = it,
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


    Box(
        modifier = modifier
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
                    drawViewModel.setCanvasResolution(it.size.height, it.size.width)
                }
                .onSizeChanged {
                    drawViewModel.setCanvasResolution(it.height, it.width)
                }
                .dragMotionEvent(
                    onDragStart = { pointerInputChange ->
                        if (isReadOnly) {
                            pointerInputChange.consume()

                        } else {
                            hasChanged = true
                            drawViewModel.touchStart(pointerInputChange)
                        }
                    },
                    onDrag = { pointerInputChange ->
                        if (isReadOnly) {
                            pointerInputChange.consume()

                        } else {
                            hasChanged = true
                            drawViewModel.touchMove(pointerInputChange)
                        }
                    },
                    onDragEnd = { pointerInputChange ->
                        if (isReadOnly) {
                            pointerInputChange.consume()
                        } else {
                            hasChanged = true
                            drawViewModel.touchUp(pointerInputChange)
                        }
                    })
        )
    }
    DrawingPropertiesMenu(
        undoCount = undoCount.value,
        redoCount = redoCount.value,
        drawMode = drawViewModel.drawMode,
        setPencilMode = { drawViewModel.setPencileMode(it) },
        onUndo = {
            drawViewModel.undo()
            hasChanged = true
        },
        onRedo = {
            drawViewModel.redo()
            hasChanged = true
        })
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
            drawPath(
                color = it.properties.drawColor(),
                path = scaledPath,
                style = Stroke(
                    width = DrawViewModel.scaleStroke(
                        currentResolution,
                        it.resolution,
                        it.properties.strokeWidth
                    )
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
    Row(
        modifier = modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(Color.White)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = {
            setPencilMode(DrawMode.Draw)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_draw_black_24dp),
                contentDescription = stringResource(id = R.string.erase),
                tint = if (drawMode == DrawMode.Draw) Color.Black else Color.LightGray
            )
        }
        IconButton(onClick = {
            setPencilMode(DrawMode.Erase)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_eraser_black_24dp),
                contentDescription = stringResource(id = R.string.erase),
                tint = if (drawMode == DrawMode.Erase) Color.Black else Color.LightGray
            )
        }
        IconButton(onClick = {
            onUndo()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo_black_24dp),
                contentDescription = stringResource(id = R.string.undo),
                tint = if (undoCount > 0) Color.Black else Color.LightGray
            )
        }

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
}

@Preview
@Composable
fun PreviewDawing() {
    Column() {

        val lines = Json.decodeFromString<List<Line>>(catTestDrawingLinesInJson)
        DrawBox(drawingLines = lines)
        DrawingPropertiesMenu(
            undoCount = 5,
            redoCount = 0,
            drawMode = DrawMode.Draw,
            onUndo = { },
            onRedo = { },
            setPencilMode = {},
        )
        Buttons(onSubmit = {}, onEnd = {})
    }
}

@Preview
@Composable
fun PreviewDawingWithSentance() {
    val lines = Json.decodeFromString<List<Line>>(catTestDrawingLinesInJson)
    Column {
        Sentence(it = "a cat winks at you with the grace of a toddler on benedryl")
        DrawBox(drawingLines = lines)
        DrawingPropertiesMenu(
            undoCount = 5,
            redoCount = 0,
            drawMode = DrawMode.Draw,
            onUndo = { },
            onRedo = { },
            setPencilMode = {},
        )
        Buttons(onSubmit = {}, onEnd = {})
    }
}