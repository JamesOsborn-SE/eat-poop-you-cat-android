package dev.develsinthedetails.eatpoopyoucat.feature.setup

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current

    // Check permission status immediately upon composition
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Pre-Tiramisu devices don't need runtime notification permission
            }
        )
    }

    val permissionLauncher: ManagedActivityResultLauncher<String, Boolean> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }

    NewGameScreen(
        hasNotificationPermission,
        permissionLauncher,
        onBack = onBack,
        onNetGame = { gameMode: GameMode ->
            viewModel.saveNewGame(gameMode)
            onNetGame(viewModel.gameId, gameMode)
        },
        onLocal = {
            viewModel.saveNewGame(GameMode.LOCAL)
            onLocal(viewModel.entryId)
        }
    )
}

@Composable
fun NewGameScreen(
    hasNotificationPermission: Boolean,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
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

                // Only show the button if they haven't granted the permission
                if (!hasNotificationPermission) {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .visible(hasNotificationPermission)
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
    val context = LocalContext.current

    // Check permission status immediately upon composition
    var hasNotificationPermission = false
    val permissionLauncher: ManagedActivityResultLauncher<String, Boolean> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }
    AppTheme {
        Surface {
            NewGameScreen(
                hasNotificationPermission = hasNotificationPermission,
                permissionLauncher = permissionLauncher,
                onBack = {},
                onNetGame = {},
                onLocal = { },
            )
        }
    }
}