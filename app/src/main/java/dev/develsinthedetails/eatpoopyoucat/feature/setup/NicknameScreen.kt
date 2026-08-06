package dev.develsinthedetails.eatpoopyoucat.feature.setup

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.Home
import dev.develsinthedetails.eatpoopyoucat.app.PreviousGame
import dev.develsinthedetails.eatpoopyoucat.app.navigateToNextTurn
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.AppButton
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.ErrorText
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NicknameScreen(
    viewModel: NicknameViewModel = koinViewModel(),
    nav: NavHostController,
) {
    val hardcodedNames = LocalResources.current.getStringArray(R.array.nicknames).toList()
    val fallbackName = stringResource(R.string.oof)

    val focusRequester = remember { FocusRequester() }
    val onContinueGame = navigateToNextTurn(navController = nav)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val useNicknames by viewModel.useNicknames.collectAsStateWithLifecycle(initialValue = false)
    if (state is EntryUiState.Loading)
        SpinnerScreen()
    else if (state is EntryUiState.Content) {
        val previousEntry = (state as EntryUiState.Content).previousEntry
        val nickname = (state as EntryUiState.Content).nickname
        if (!useNicknames)
            onContinueGame(previousEntry, nickname)
        else {
            viewModel.validateAndAutoAssignNickname(hardcodedNames, fallbackName)
            NicknameScreen(
                nickname = nickname,
                previousNicknames = (state as EntryUiState.Content).previousNicknames,
                onChange = { viewModel.updateNickname(it) },
                onSubmit = {
                    if (viewModel.validateAndAutoAssignNickname(hardcodedNames, fallbackName)) {
                        onContinueGame(previousEntry, nickname)
                    }
                },
                onEnd = {
                    nav.navigate(PreviousGame(previousEntry.gameId)) {
                        popUpTo(Home)
                    }
                },
                nicknameError = (state as EntryUiState.Content).nicknameError,
                focusRequester = focusRequester
            )
        }
    }
}

@Composable
fun NicknameScreen(
    nickname: String,
    previousNicknames: List<String>,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onEnd: () -> Unit,
    nicknameError: Int?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Scaffolds.InGame(
        title = stringResource(R.string.new_player_prompt),
        onEnd = onEnd
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(ScrollState(0)),
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.nickname_prompt),
                        Modifier.padding(bottom = 24.dp),
                        fontSize = 20.sp
                    )
                }
                if (previousNicknames.isNotEmpty()) {
                    Text(stringResource(R.string.previous_nicknames))
                    Column(modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)) {
                        previousNicknames.forEach { message ->
                            Text(message)
                        }
                    }
                }
                if (nicknameError != null) {
                    ErrorText(true, stringResource(nicknameError))
                }
                Column {
                    Row {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = onChange,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onSubmit() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(.6f)
                                .focusRequester(focusRequester),
                            enabled = true,
                            readOnly = false,
                            shape = RoundedCornerShape(8.dp),

                            label = {
                                Text(
                                    stringResource(R.string.enter_nickname_prompt),
                                    modifier = modifier
                                )
                            },
                        )
                    }
                    Row(modifier = Modifier) {
                        AppButton(
                            imageVector = Icons.Rounded.PersonAddAlt,
                            modifier = modifier.fillMaxWidth(),
                            text = R.string.that_s_me,
                            iconDescription = R.string.that_s_me,
                            onClick = onSubmit,
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreview() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameScreen(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {}, {},
                null,
                focusRequester
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreviewEmpty() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameScreen(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {}, {},
                R.string.no_nickname_chosen_warning,
                focusRequester
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreviewEmptyNobody() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = listOf<String>()
    AppTheme {
        Surface {
            NicknameScreen(
                "",
                listOfNicknames,
                {},
                {}, {},
                null,
                focusRequester
            )
        }
    }
}