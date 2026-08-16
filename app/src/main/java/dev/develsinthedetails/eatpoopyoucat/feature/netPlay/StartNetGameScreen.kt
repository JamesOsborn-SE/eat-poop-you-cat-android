package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import dev.develsinthedetails.eatpoopyoucat.core.utilities.valueOrEmpty
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Server
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

data class ShareData(
    val link: String,
    val nickname: String?,
    val onChangeNickname: (String) -> Unit,
    val timeout: Int,
    val onChangeTimeout: (String) -> Unit,
    val turnTimeout: Int,
    val onChangeTurnTimeOut: (String) -> Unit,
)

@Composable
fun SelectableReadOnlyTextWithShare(link: String) {
    val context = LocalContext.current
    OutlinedTextField(
        value = link,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Link to share:") },
        trailingIcon = {
            IconButton(
                onClick = {
                    // Standard Android Share Intent
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        // Todo change link to R string blurb
                        putExtra(Intent.EXTRA_TEXT, link)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share via")
                    context.startActivity(shareIntent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Game"
                )
            }
        }
    )
}

@Composable
fun StartNetGameScreen(
    viewModel: StartNetGameViewModel = koinViewModel(),
    appSettings: AppSettings = koinInject(),
    onStartGame: (Uuid) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val player by viewModel.player.collectAsStateWithLifecycle()
    val nickname = player?.nickname
    var currentIp by remember {
        mutableStateOf(NetworkUtils.getLocalIpAddress()?.let { "$it:3947" } ?: "Server Offline")
    }

    val link = "${appSettings.playDeepLink}/${currentIp}/${viewModel.gameId}"
    val serverAction by viewModel.serverAction.collectAsState()

    LaunchedEffect(Unit) {
        val isWifiOn = NetworkUtils.isWifiConnected(context)
        val ipAddress = viewModel.address
        viewModel.onStartServerRequested(isWifiOn, ipAddress)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val ipAddress = NetworkUtils.getLocalIpAddress()
            currentIp = if (ipAddress != null) {
                "$ipAddress:3947"
            } else {
                "Server Offline"
            }
            delay(1.seconds)
        }
    }

    LaunchedEffect(serverAction) {
        when (val action = serverAction) {
            is StartNetGameViewModel.ServerAction.StartService -> {
                val serviceIntent = Intent(context, Server::class.java)
                context.startService(serviceIntent)
                viewModel.resetAction()
            }

            is StartNetGameViewModel.ServerAction.PromptWifiTurnOn -> {
                val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_WIFI)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
                context.startActivity(panelIntent)
                viewModel.resetAction()
            }

            StartNetGameViewModel.ServerAction.Idle -> { /* Do nothing */
            }
        }
    }

    // todo fill in onChange*
    val sd = ShareData(link, nickname, onChangeNickname = {
        viewModel.validateNickname(it)
    }, 5, {}, 5, {}
    )
    ShareGame(
        sd,
        !nickname.isNullOrBlank() && currentIp != "Server Offline",
        onBack,
        onStartGame = {
            viewModel.createRoster()
            onStartGame(viewModel.gameId)
        })
}

@Composable
fun ShareGame(
    shareData: ShareData,
    canStart: Boolean,
    onBack: () -> Unit,
    onStartGame: () -> Unit,
) {
    Scaffolds.Backable(
        "Let's go ${shareData.nickname.valueOrEmpty()}!",
        onBack,
        floatingActionButton = {
            Button(onClick = onStartGame, enabled = canStart) {
                Text(stringResource(R.string.start))
            }
        }) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                OutlinedTextField(
                    value = shareData.nickname.valueOrEmpty(),
                    onValueChange = shareData.onChangeNickname,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { shareData.onChangeNickname }),
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = true,
                    readOnly = false,
                    maxLines = 1,
                    shape = RoundedCornerShape(8.dp),

                    label = {
                        Text("Change you nickname?")
                    },
                )
                OutlinedTextField(
                    value = shareData.timeout.toString(),
                    onValueChange = shareData.onChangeTimeout,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { shareData.onChangeTimeout }),
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = true,
                    readOnly = false,
                    maxLines = 1,
                    shape = RoundedCornerShape(8.dp),

                    label = {
                        Text("Timeout to accept game")
                    },
                )
                OutlinedTextField(
                    value = shareData.turnTimeout.toString(),
                    onValueChange = shareData.onChangeTurnTimeOut,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { shareData.onChangeTurnTimeOut }),
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = true,
                    readOnly = false,
                    maxLines = 1,
                    shape = RoundedCornerShape(8.dp),

                    label = {
                        Text("Timeout for a turn")
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(20.dp))
                SelectableReadOnlyTextWithShare(shareData.link)
                HorizontalDivider(modifier = Modifier.padding(20.dp))
                Text("wall of words explaining stuff")
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShareGamePreview() {
    val sd = ShareData(
        link = "epyc://play/192.168.1.10:3947/${Uuid.NIL}",
        nickname = "Muthafucka",
        {}, 5, {}, 10, {})
    AppTheme {
        ShareGame(sd, true, {},{})
    }
}
