package dev.develsinthedetails.eatpoopyoucat.compose.previousgames

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGamesViewModel

@Composable
fun PreviousGamesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreviousGamesViewModel = hiltViewModel(),
    onGameClick: (String) -> Unit,
) {
    val games by viewModel.games.observeAsState(initial = emptyList())
    PreviousGamesScreen(games = games, modifier, onGameClick = onGameClick)
}

@Composable
fun PreviousGamesScreen(
    games: List<GameWithEntries>,
    modifier: Modifier = Modifier,
    onGameClick: (String) -> Unit = {},
) {
    EatPoopYouCatTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier.testTag("game_list"),
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.card_side_margin),
                    vertical = dimensionResource(id = R.dimen.header_margin)
                )
            ) {
                items(
                    items = games,
                    key = { it.game.id }
                ) { game ->
                    GameListItem(game = game) {
                        onGameClick(game.game.id.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun GameListItem(game: GameWithEntries, onClick: () -> Unit) {
    val firstSentence = game.entries
        .minBy { it.sequence }.sentence ?: String()
    val lastDrawing = game.entries
        .sortedBy { it.sequence }
        .lastOrNull { it.drawing != null }
    ListItem(
        sentence = firstSentence,
        drawing = lastDrawing?.drawing,
        turns = game.entries.count(),
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItem(
    sentence: String,
    drawing: ByteArray?,
    turns: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.card_side_margin))
            .padding(bottom = dimensionResource(id = R.dimen.card_bottom_margin))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.margin_normal))
        ) {
            Text(
                text = sentence,
                textAlign = TextAlign.Start,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
            )
            Text(
                text = "${stringResource(id = R.string.turns)}: $turns",
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            )

            if (drawing != null) {
                DrawBox(
                    drawingZippedJson = drawing,
                    onClick = onClick
                )
            }
        }
    }
}