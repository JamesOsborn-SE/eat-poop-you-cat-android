package dev.develsinthedetails.eatpoopyoucat.compose.draw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun DrawScreen(
    viewModel: DrawViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
) {
    EatPoopYouCatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (viewModel.isLoading)
                Spinner()
            Column {
                val previousEntry by viewModel.previousEntry.observeAsState()
                previousEntry?.sentence?.let {
                    Column {
                        Text(stringResource(R.string.draw_this_sentence))
                        Text(it)
                    }
                }
                Draw()

                if (viewModel.isError) {
                    Text(text = "make a more better picture.", color = Color.Red)
                }

                Button(onClick = {
                    viewModel.checkDrawing { onNavigateToSentence(viewModel.entryId) }

                }) {
                    Text(stringResource(R.string.accept))
                }
            }
        }
    }
}


@Composable
fun Draw(
    drawViewModel: DrawViewModel = hiltViewModel(),
    isReadOnly: Boolean = false,
    drawingLines: ArrayList<Line> = ArrayList(),
    entryResolution: Resolution? = null
) {

    drawViewModel.isReadOnly = isReadOnly

    if (drawingLines.isNotEmpty()) {
        drawViewModel.drawingPaths = Drawing(drawingLines).toPaths()
    }

    var hasChanged by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(all = 8.dp)
            .background(Color.White)
            .aspectRatio(1f)
            .fillMaxWidth()
            .onPlaced {
                drawViewModel.canvasHeight = it.size.height
                drawViewModel.canvasWidth = it.size.width
            }
            .onSizeChanged {
                drawViewModel.canvasHeight = it.height
                drawViewModel.canvasWidth = it.width
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
            .drawBehind {
                if (hasChanged) {
                    drawViewModel.doDraw(this, true, entryResolution)
                    hasChanged = false
                } else {
                    drawViewModel.doDraw(this, false, entryResolution)
                }
            }

    )

    DrawingPropertiesMenu(
        modifier = Modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(Color.White)
            .padding(4.dp),
        undoCount = drawViewModel.undoCount,
        redoCount = drawViewModel.redoCount,
        onUndo = {
            drawViewModel.undo()
            hasChanged = true
        },
        onRedo = {
            drawViewModel.redo()
            hasChanged = true
        }
    )
}

@Composable
fun DrawReadOnly(
    drawingZippedJson: ByteArray,
    entryResolution: Resolution? = null,
    onClick: () -> Unit = {},
) {
    val lines: List<Line> =
        Json.decodeFromString(Gzip.decompressToString(drawingZippedJson))
    val drawingPaths = Drawing(lines).toPaths()
    var height = 0
    var width = 0
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(all = 8.dp)
            .background(color = Color.White)
            .clickable { onClick.invoke() }
            .onPlaced {
                height = it.size.height
                width = it.size.width
            }
            .onSizeChanged {
                height = it.height
                width = it.width
            }
            .drawBehind {
                val currentResolution = Resolution(height = height, width = width)
                DrawViewModel.doDraw(
                    drawingPaths = drawingPaths,
                    drawScope = this,
                    currentPath = Path(),
                    currentResolution = currentResolution,
                    originalResolution = entryResolution,
                    hasChanged = false
                )
            }
    )
}

@Composable
private fun DrawingPropertiesMenu(
    modifier: Modifier = Modifier,
    undoCount: Int = 0,
    redoCount: Int = 0,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = {
            onUndo()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo_black_24dp),
                contentDescription = "Undo",
                tint = if (undoCount > 0) Color.Black else Color.LightGray
            )
        }

        IconButton(onClick = {
            onRedo()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_redo_black_24dp),
                contentDescription = "Redo",
                tint = if (redoCount > 0) Color.Black else Color.LightGray
            )
        }
    }
}
