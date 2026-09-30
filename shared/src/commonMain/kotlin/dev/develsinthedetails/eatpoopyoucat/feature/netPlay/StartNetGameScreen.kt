package dev.develsinthedetails.eatpoopyoucat.feature.netPlay


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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareLink
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ManageServerLifecycle
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.ic_share_filled
import eatpoopyoucat.shared.generated.resources.start
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid


@Composable
fun SelectableReadOnlyTextWithShare(modifier: Modifier = Modifier, link: String) {
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
                    shareLink(link)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_share_filled),
                    contentDescription = "Share Game"
                )
            }
        }
    )
}

@Composable
fun StartNetGameScreen(
    viewModel: StartNetGameViewModel = koinViewModel(),
    serverManager: ServerManager = koinInject(),
    onStartGame: (Uuid) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ManageServerLifecycle(
        serverManager = serverManager,
        onUpdateAddress = { viewModel.updateAddress(it) }
    )
    ShareGame(
        uiState,
        onNickNameChange = { viewModel.updateNickname(it) },
        { viewModel.updateTurnTimeOut(it) },
        { viewModel.updateTimeOut(it) },
        onBack,
        onStartGame = {
            viewModel.startNetGame()
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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    val canStart = !uiState.nickname.isBlank() && uiState.address != "Server Offline"
    val displayName = uiState.nickname.ifBlank { "Pick a name!!" }
    Scaffolds.Backable(
        "Let's go $displayName!",
        onBack,
        floatingActionButton = {
            Button(onClick = onStartGame, enabled = canStart) {
                Text(stringResource(Res.string.start))
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
                    value = uiState.nickname,
                    onValueChange = {
                        onNickNameChange(it)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = null
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = true,
                    readOnly = false,
                    maxLines = 1,
                    shape = RoundedCornerShape(8.dp),

                    label = {
                        Text("Your nickname")
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
                        onNext = null
                    ),
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
                        onNext = null
                    ),
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


@Preview
@Composable
fun ShareGamePreview() {
    val sd = NewNetGameUiState(
        Uuid.NIL, GameMode.LAN, Uuid.NIL,
        address = "http://192.168.1.10:$SERVER_PORT",
    )
    AppTheme {
        ShareGame(sd, {}, {}, {}, {}, {})
    }
}
