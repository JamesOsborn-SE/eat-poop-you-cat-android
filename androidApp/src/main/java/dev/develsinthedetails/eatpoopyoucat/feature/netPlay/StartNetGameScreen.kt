package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareEncode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.valueOrEmpty
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ManageServerLifecycle
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.Uuid

fun getShareLink(deepLink: String, address: String, gameId: Uuid): String {
    return "${deepLink}/?game=${gameId.shareEncode()}&server=${address.shareEncode()}"
}

@Composable
fun SelectableReadOnlyTextWithShare(modifier: Modifier = Modifier, link: String) {
    val context = LocalContext.current
    OutlinedTextField(
        value = link,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Link to share:") },
        trailingIcon = {
            IconButton(
                onClick = {
                    // Standard Android Share Intent
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, link)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share via")
                    context.startActivity(shareIntent)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share_filled),
                    contentDescription = "Share Game"
                )
            }
        }
    )
}

@Composable
fun StartNetGameScreen(
    viewModel: StartNetGameViewModel = koinViewModel(),
    onStartGame: (Uuid) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ManageServerLifecycle( onUpdateAddress = { viewModel.updateAddress(it) } )
    ShareGame(
        uiState,
        onNickNameChange = { viewModel.updateNickname(it) },
        { viewModel.updateTurnTimeOut(it) },
        { viewModel.updateTimeOut(it) },
        onBack,
        onStartGame = {
            viewModel.createRoster()
            onStartGame(uiState.gameId)
        })
}

@Composable
fun ShareGame(
    uiState: NewNetGameUiState,
    onNickNameChange: (String) -> Unit,
    onChangeTurnTimeOut: (String) -> Unit,
    onChangeTimeout: (String) -> Unit,
    onBack: () -> Unit,
    onStartGame: () -> Unit,
) {
    val canStart = !uiState.player.nickname.isBlank() && uiState.address != "Server Offline"

    Scaffolds.Backable(
        "Let's go ${uiState.player.nickname.ifBlank { "Pick a name!!" }}!",
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
                    value = uiState.player.nickname.valueOrEmpty(),
                    onValueChange = onNickNameChange,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = null),
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
                    value = uiState.timeout.toString(),
                    onValueChange = onChangeTimeout,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = null),
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
                    value = uiState.turnTimeout.toString(),
                    onValueChange = onChangeTurnTimeOut,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = null),
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
                Text("wall of words explaining stuff")
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShareGamePreview() {
    val sd = NewNetGameUiState(
        Uuid.NIL, GameMode.LAN, Player(Uuid.NIL, nickname = "Muthafucka"),
        address = "http://192.168.1.10:3947",
    )
    AppTheme {
        ShareGame(sd,  {}, {}, {}, {}, {})
    }
}
