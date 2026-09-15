package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.rememberNotificationPermissionState
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.dialog_start_game
import eatpoopyoucat.shared.generated.resources.ic_lan
import eatpoopyoucat.shared.generated.resources.ic_phone_android_rounded
import eatpoopyoucat.shared.generated.resources.ic_wifi
import eatpoopyoucat.shared.generated.resources.new_game
import eatpoopyoucat.shared.generated.resources.next
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun NewGameScreen(
    viewModel: NewGameViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNewGame: (Uuid, GameMode) -> Unit,
) {
    val permissionState = rememberNotificationPermissionState()

    NewGameScreen(
        hasNotificationPermission = permissionState.hasPermission,
        onRequestPermission = { permissionState.requestPermission() },
        onBack = onBack,
        onNewGame = { gameMode: GameMode ->
            viewModel.saveNewGame(gameMode, onNewGame)
        }
    )
}

@Composable
fun NewGameScreen(
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    onNewGame: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffolds.Backable(
        title = stringResource(Res.string.new_game), onBack = onBack
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
                        painter = painterResource(Res.drawable.ic_phone_android_rounded),
                        contentDescription = stringResource(Res.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text("Play by passing this device", textAlign = TextAlign.Center)
                    StartGame(
                        { onNewGame(GameMode.LOCAL) },
                        stringResource(Res.string.dialog_start_game),
                        defaultModifier
                    )
                }
                HorizontalDivider(Modifier.padding(20.dp), 3.dp)

                // Only show the button if they haven't granted the permission
                if (!hasNotificationPermission) {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = onRequestPermission
                    ) {
                        Text("Turn on notifications for multi device play?")
                    }
                    return@Surface
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .visible(hasNotificationPermission)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_lan),
                        contentDescription = stringResource(Res.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text("Play on multiple devices on a shared network")
                    StartGame(onStartGame = {
                        onNewGame(GameMode.LAN)
                    }, stringResource(Res.string.next), defaultModifier)
                }

                HorizontalDivider(Modifier.padding(20.dp).visible(false), 3.dp)

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .visible(false)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_wifi),
                        contentDescription = stringResource(Res.string.dialog_start_game),
                        modifier = Modifier
                            .size(iconSize)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Text(
                        "Play with friends online",
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                    StartGame(onStartGame = {
                        onNewGame(GameMode.INET)
                    }, stringResource(Res.string.next), defaultModifier)
                }
            }
        }
    }
}

@Composable
fun StartGame(
    onStartGame: () -> Unit,
    startText: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onStartGame, modifier = modifier
    ) {
        Text(startText)
    }
}

@Preview
@Composable
fun NewGameNoNotificationPreview() {
    val hasNotificationPermission = false
    AppTheme {
        Surface {
            NewGameScreen(
                hasNotificationPermission = hasNotificationPermission,
                onBack = {},
                onNewGame = {},
                onRequestPermission = {},
            )
        }
    }
}

@Preview
@Composable
fun NewGameYesNotificationPreview() {
    val hasNotificationPermission = true
    AppTheme {
        Surface {
            NewGameScreen(
                hasNotificationPermission = hasNotificationPermission,
                onRequestPermission = {},
                onBack = {},
                onNewGame = {},
            )
        }
    }
}