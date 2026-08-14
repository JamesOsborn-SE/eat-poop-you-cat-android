package dev.develsinthedetails.eatpoopyoucat.feature.setup


import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid


@Composable
fun NewGameScreen(
    viewModel: NewGameViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNetGame: (Uuid, GameMode) -> Unit,
    onLocal: (Uuid) -> Unit,
) {
    NewGameScreen(onBack, onNetGame = { gameMode ->
        viewModel.saveNewGame(gameMode)
        onNetGame(viewModel.gameId, gameMode)
    }, onLocal = {
        viewModel.saveNewGame(GameMode.LOCAL)
        onLocal(viewModel.entryId)
    })
}

@Composable
fun NewGameScreen(
    onBack: () -> Unit,
    onNetGame: (GameMode) -> Unit,
    onLocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffolds.Backable(
        title = stringResource(R.string.new_game), onBack = onBack
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(ScrollState(0)),
        ) {
            val iconSize = 75.dp
            Column {
                val defaultModifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 20.dp)
                ) {
                    Icon(
                        Icons.Rounded.PhoneAndroid,
                        contentDescription = stringResource(id = R.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text("Play by passing this device", textAlign = TextAlign.Center)
                    StartGame(onLocal, defaultModifier)
                }
                HorizontalDivider(Modifier.padding(20.dp), 3.dp)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Rounded.Lan,
                        contentDescription = stringResource(id = R.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text("Play on multiple devices on a shared network")
                    Next(onStartGame = {
                        onNetGame(GameMode.LAN)
                    }, defaultModifier)

                }
                HorizontalDivider(Modifier.padding(20.dp), 3.dp)
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {

                    Icon(
                        Icons.Rounded.Wifi,
                        contentDescription = stringResource(id = R.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text(
                        "Play with friends online",
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                    Next(onStartGame = {
                        onNetGame(GameMode.INET)
                    }, defaultModifier)

                }
            }
        }
    }
}
@Composable
fun StartGame(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onStartGame, modifier = modifier
    ) {
        Text(stringResource(id = R.string.dialog_start_game))
    }
}

@Composable
fun Next(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onStartGame, modifier = modifier
    ) {
        Text(stringResource(R.string.next))
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NewGamePreview() {
    AppTheme {
        Surface {
            NewGameScreen(
                onBack = {},
                onNetGame = {},
                onLocal = { },
            )
        }
    }
}
