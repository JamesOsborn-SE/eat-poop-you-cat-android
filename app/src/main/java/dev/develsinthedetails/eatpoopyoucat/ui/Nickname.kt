package dev.develsinthedetails.eatpoopyoucat.ui

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.AppButton
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.ErrorText
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen
import dev.develsinthedetails.eatpoopyoucat.viewmodels.NicknameViewModel

@Composable
fun Nickname(
    viewModel: NicknameViewModel = hiltViewModel(),
    nav: NavHostController,
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val onContinueGame = navigateToNextTurn(navController = nav)

    Scaffolds.InGame(
        title = "New player; Who dis?",
        onEnd = {
            nav.navigate(Screen.Game.byId(viewModel.previousEntry!!.gameId.toString())){
                popUpTo(Screen.Home.route)
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(ScrollState(0)),
        ) {
            if (viewModel.isLoading) {
                Spinner()
            } else {
                Nickname(
                    nickname = viewModel.nickname,
                    previousNicknames = viewModel.previousNicknames,
                    onChange = { viewModel.updateNickname(it) },
                    onSubmit = {
                        if (viewModel.isValidNickname(context)) {
                            SharedPref.write("nickname", viewModel.nickname.trim())
                            onContinueGame(viewModel.previousEntry!!)
                        }
                    },
                    isError = viewModel.isError,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
fun Nickname(
    nickname: String,
    previousNicknames: List<String>,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isError: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.PersonPin,null,
            Modifier.size(100.dp),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
        if (previousNicknames.isNotEmpty()) {
            Text("Previous nicknames:")
            Column(modifier = Modifier.padding(10.dp)) {
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


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicnamePreview() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = listOf(
        "Not James",
        "Knucklebutt",
        "Hot Sauce",
        "Pebbles",
        "Sassy",
        "Shrinkwrap",
        "Buds",
        "Pickles"
    )
    AppTheme {
        Surface {
            Nickname(
                "oofster",
                listOfNicknames,
                {},
                {},
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
    val listOfNicknames = listOf(
        "Not James",
        "Knucklebutt",
        "Hot Sauce",
        "Pebbles",
        "Sassy",
        "Shrinkwrap",
        "Buds",
        "Pickles"
    )
    AppTheme {
        Surface {
            Nickname(
                "",
                listOfNicknames,
                {},
                {},
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
            Nickname(
                "",
                listOfNicknames,
                {},
                {},
                false,
                focusRequester
            )
        }
    }
}