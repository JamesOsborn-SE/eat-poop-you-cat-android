package dev.develsinthedetails.eatpoopyoucat.feature.netplay


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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.Screen
import dev.develsinthedetails.eatpoopyoucat.app.SharedPref
import dev.develsinthedetails.eatpoopyoucat.app.navigateToNextTurn
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.AppButton
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.ErrorText
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.SpinnerScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanGameScreen(
    viewModel: NetGameViewModel = koinViewModel(),
    nav: NavHostController) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val onContinueGame = navigateToNextTurn(navController = nav)
    val previousEntry = viewModel.previousEntry

    if (viewModel.isLoading)
        SpinnerScreen()
    else {
        if (!SharedPref.useNicknames() && previousEntry != null)
            onContinueGame(previousEntry)
        else
            LanGameScreen(
                nickname = viewModel.nickname,
                previousNicknames = viewModel.previousNicknames,
                onChange = { viewModel.updateNickname(it) },
                onSubmit = {
                    if (viewModel.isValidNickname(context)) {
                        SharedPref.write(SharedPref.NICKNAME, viewModel.nickname.trim())
                        onContinueGame(viewModel.previousEntry!!)
                    }
                },
                onEnd = {
                    nav.navigate(Screen.Game(viewModel.previousEntry!!.gameId)) {
                        popUpTo(Screen.Home)
                    }
                },
                isError = viewModel.isError,
                focusRequester = focusRequester
            )
    }
}

@Composable
fun LanGameScreen(
    nickname: String,
    previousNicknames: List<String>,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onEnd: () -> Unit,
    isError: Boolean,
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
                    Text(text = stringResource(R.string.nickname_prompt), Modifier.padding(bottom = 24.dp), fontSize = 20.sp)
                }
                if (previousNicknames.isNotEmpty()) {
                    Text(stringResource(R.string.previous_nicknames))
                    Column(modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)) {
                        previousNicknames.forEach { message ->
                            Text(message)
                        }
                    }
                }

                ErrorText(isError, stringResource(R.string.no_nickname_chosen_warning))
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
