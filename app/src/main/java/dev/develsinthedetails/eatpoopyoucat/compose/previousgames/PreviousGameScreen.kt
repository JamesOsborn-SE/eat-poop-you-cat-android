package dev.develsinthedetails.eatpoopyoucat.compose.previousgames

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGameViewModel

@Composable
fun PreviousGameScreen(
    modifier: Modifier = Modifier,
    viewModel: PreviousGameViewModel = hiltViewModel(),
) {
    val game by viewModel.gameWithEntries.observeAsState()
    game?.let { PreviousGameScreen(entries = it.entries, modifier) }
}

@Composable
fun PreviousGameScreen(
    entries: List<Entry>,
    modifier: Modifier = Modifier,
) {
    EatPoopYouCatTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = modifier.testTag("Entry_list"),
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.card_side_margin),
                    vertical = dimensionResource(id = R.dimen.header_margin)
                )
            ) {
                items(
                    items = entries,
                    key = { it.id }
                ) {
                    EntryListItem(it)
                }
            }
        }
    }
}

@Composable
fun EntryListItem(entry: Entry) {
    val sentence = entry.sentence
    val drawing = entry.drawing

    if (sentence != null) {

        Text(
            text = sentence,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
        )
    }

    if (drawing != null) {
        DrawBox(
            drawingZippedJson = drawing,
        )

    }
}
