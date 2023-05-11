package dev.develsinthedetails.eatpoopyoucat.compose.home

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.GreetingViewModel
import java.util.UUID


@Composable
fun HomeScreen(
    viewModel: GreetingViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
    onNavigateToPreviousGames: () -> Unit,
) {
    HomeScreen(
        userName = viewModel.userName,
        isLoading = viewModel.isLoading,
        onNickNameValueChange = { viewModel.updateNickName(it) },
        onStartGame = {
            val entryId = UUID.randomUUID()
            viewModel.saveNewGame(
                entryId
            ) { onNavigateToSentence(entryId.toString()) }
        },
        onNavigateToPreviousGames = onNavigateToPreviousGames,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String,
    isLoading: Boolean,
    onNickNameValueChange: (String) -> Unit,
    onStartGame: () -> Unit,
    onNavigateToPreviousGames: () -> Unit,
) {
    val padding = 20.dp
    EatPoopYouCatTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading)
                Spinner()
            Column {
                val defaultModifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(padding)
                Column(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(padding)) {
                    Text(
                        text = stringResource(R.string.welcome_message),
                        modifier = defaultModifier,
                        fontSize = 18.sp
                    )
                    if (0 == 1) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                onNickNameValueChange(it)
                            },
                            modifier = defaultModifier,
                            enabled = true,
                            readOnly = false,
                            shape = RoundedCornerShape(8.dp),

                            label = {
                                Text(
                                    stringResource(R.string.nickname),
                                    modifier = modifier
                                )
                            },
                        )
                    }
                    StartGame(defaultModifier, onStartGame)
                    ViewPreviousGames(defaultModifier, onNavigateToPreviousGames)
                    Text(text = stringResource(id = R.string.app_description), modifier = defaultModifier)
                }
            }
        }
    }
}

@Composable
fun ViewPreviousGames(modifier: Modifier, navTo: () -> Unit) {
    Button(
        modifier=modifier,
        onClick = {
            navTo()
        }) {
        Text(stringResource(id = R.string.previous_games))
    }
}

@Composable
fun StartGame(
    modifier: Modifier,
    onStartGame: () -> Unit,
) {
    Button(onClick = onStartGame,
        modifier = modifier ) {
        Text(stringResource(id = R.string.dialog_start_game))
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun HomeScreenDarkPreview() {
    HomeScreen(
        userName = "Me",
        isLoading = false,
        onNickNameValueChange = {},
        onStartGame = {},
        onNavigateToPreviousGames = {})
}



@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        userName = "Me",
        isLoading = false,
        onNickNameValueChange = {},
        onStartGame = {},
        onNavigateToPreviousGames = {})
}
