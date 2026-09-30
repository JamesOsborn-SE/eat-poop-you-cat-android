package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.secondaryButtonColors
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.ask_join
import eatpoopyoucat.shared.generated.resources.no
import eatpoopyoucat.shared.generated.resources.oof
import eatpoopyoucat.shared.generated.resources.yes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid


@Composable
fun JoinNetGameScreen(
    viewModel: JoinNetGameViewModel = koinViewModel(),
    gameId: Uuid,
    onBack: () -> Unit,
    toInProgressGame: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    viewModel.initFromDeepLink(gameId)
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Spinner()
        }
    } else {
        val joinData = JoinData(
            onChangeNickname = { newName -> viewModel.updateNickname(newName) },
            onYesPlay = { viewModel.onYesPlay() },
            onNoPlay = { onBack() }
        )

        JoinNetGameScreen(
            uiState,
            toInProgressGame = toInProgressGame,
            joinData = joinData,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
fun JoinNetGameScreen(
    uiState: JoinUiState,
    joinData: JoinData,
    toInProgressGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isInGameAlready) {
        toInProgressGame()
    }
    AppTheme {
        Scaffolds.Backable(stringResource(Res.string.ask_join), onBack = joinData.onNoPlay) { pad ->
            Column(modifier.padding(pad)) {
                OutlinedTextField(
                    value = uiState.nickname,
                    onValueChange = joinData.onChangeNickname,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { joinData.onChangeNickname }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 20.dp),
                    enabled = true,
                    readOnly = false,
                    maxLines = 1,
                    shape = RoundedCornerShape(8.dp),

                    label = {
                        Text("Change you nickname?")
                    },
                )
                val m = Modifier
                    .fillMaxWidth(.3f)
                    .align(Alignment.CenterHorizontally)
                Button(joinData.onYesPlay, content = {
                    Text(stringResource(Res.string.yes))
                }, modifier = m)

                Button(joinData.onNoPlay, content = {
                    Text(stringResource(Res.string.no))
                }, colors = secondaryButtonColors(), modifier = m)
            }
        }
    }
}

data class JoinData(
    val onChangeNickname: (String) -> Unit,
    val onYesPlay: () -> Unit,
    val onNoPlay: () -> Unit
)

@Preview
@Composable
fun JoinNetGamePreview() {
    val d = JoinData({}, {}, {})
    JoinNetGameScreen(
        JoinUiState(playerId = Uuid.NIL,stringResource(Res.string.oof)), d,
        toInProgressGame = {},
    )
}