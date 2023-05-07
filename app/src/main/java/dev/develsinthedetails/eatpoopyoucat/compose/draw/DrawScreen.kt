package dev.develsinthedetails.eatpoopyoucat.compose.draw

import android.graphics.Path
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.views.DrawView
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
                previousEntry?.sentence?.let { Text(it) }
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
    drawingLines: ArrayList<Path> = ArrayList(),
    drawingZippedJson: ByteArray? = null
) {
    drawViewModel.isReadOnly = isReadOnly

    if (drawingLines.isNotEmpty()) {
        drawViewModel.drawingPaths = drawingLines
    }

    if (drawingZippedJson != null) {
        val lines: MutableList<Line> = Json.decodeFromString(Gzip.decompressToString(drawingZippedJson))
        drawViewModel.drawingPaths = Drawing(lines).toPaths()
    }

    // Adds view to Compose
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(all = 8.dp)
            .background(color = Color.White)
    ) {
        AndroidView(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(all = 8.dp)
                .onPlaced {
                    drawViewModel.height = it.size.height
                    drawViewModel.width = it.size.width
                }
                .onSizeChanged {
                    drawViewModel.height = it.height
                    drawViewModel.width = it.width
                },
            factory = { context ->
                // Creates view
                DrawView(
                    context = context,
                    attributeSet = null,
                    drawingPaths = drawViewModel.drawingPaths,
                    undonePaths = drawViewModel.undonePaths,
                    lineSegments = drawViewModel.lineSegments,
                    drawingLines = drawViewModel.drawingLines,
                    undoneLines = drawViewModel.undoneLines,
                    isReadOnly = drawViewModel.isReadOnly,
                    justCleared = drawViewModel.justCleared,
                    originalResolution = null,
                )
            },
            update = { view ->
                drawViewModel.drawingPaths = view.drawingPaths
                drawViewModel.undonePaths = view.undonePaths
                drawViewModel.lineSegments = view.lineSegments
                drawViewModel.drawingLines = view.drawingLines
                drawViewModel.undoneLines = view.undoneLines
                drawViewModel.isReadOnly = view.isReadOnly
                drawViewModel.justCleared = view.justCleared
            }
        )
    }

}
@Composable
fun DrawReadOnly(
    drawingZippedJson: ByteArray,
    entryResolution: Resolution,
    onClick: () -> Unit= { }
) {
    val drawingLines: MutableList<Line> = Json.decodeFromString(Gzip.decompressToString(drawingZippedJson))

    // Adds view to Compose
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(all = 8.dp)
            .background(color = Color.White)
    ) {
        AndroidView(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(all = 8.dp),
            factory = { context ->
                // Creates view
                DrawView(
                    context,
                    attributeSet = null,
                    drawingPaths = Drawing(drawingLines).toPaths(),
                    isReadOnly = true,
                    originalResolution = entryResolution,
                ).apply {
                    // Sets up listeners for View -> Compose communication
                    setOnClickListener {
                    }
                }
            },
            update = { }
        )
        // HACK to make the drawing clickable
        Text(text = "",
            Modifier
                .fillMaxSize()
                .clickable { onClick.invoke() })
    }

}