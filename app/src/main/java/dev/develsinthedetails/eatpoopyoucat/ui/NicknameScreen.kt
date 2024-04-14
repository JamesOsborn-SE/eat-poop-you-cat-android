package dev.develsinthedetails.eatpoopyoucat.ui

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.AppButton
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.ErrorText
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen
import dev.develsinthedetails.eatpoopyoucat.viewmodels.NicknameViewModel

@Composable
fun NicknameScreen(
    viewModel: NicknameViewModel = hiltViewModel(),
    nav: NavHostController,
) {
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
                    NicknameScreen(
                        nickname = viewModel.nickname,
                        previousNicknames = viewModel.previousNicknames,
                        onChange = { viewModel.updateNickname(it) },
                        onSubmit = {
                            if (viewModel.isValidNickname(context)) {
                                SharedPref.write("nickname", viewModel.nickname.trim())
                                onContinueGame(viewModel.previousEntry!!)
                            }
                        },
                        onEnd = {
                            nav.navigate(Screen.Game.byId(viewModel.previousEntry!!.gameId.toString())) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        isError = viewModel.isError,
                        focusRequester = focusRequester
                    )
                }

 }

@Composable
fun NicknameScreen(
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
        title = "New player; Who dis?",
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
                    Text(text = "What do they call you around here?", Modifier.padding(bottom = 24.dp), fontSize = 20.sp)
                }
                if (previousNicknames.isNotEmpty()) {
                    Text("Previous nicknames:")
                    Column(modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)) {
                        previousNicknames.forEach { message ->
                            Text(message)
                        }
                    }
                }

                ErrorText(isError, "Fine, I'll pick one for you")
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
                                    "Enter your name or nickname",
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
fun NicnamePreview() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameScreen(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {},{},
                false,
                focusRequester
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicnamePreviewEmpty() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameScreen(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {},{},
                true,
                focusRequester
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicnamePreviewEmptyNobody() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = listOf<String>()
    AppTheme {
        Surface {
            NicknameScreen(
                "",
                listOfNicknames,
                {},
                {},{},
                false,
                focusRequester
            )
        }
    }
}