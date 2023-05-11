package dev.develsinthedetails.eatpoopyoucat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.GreetingViewModel
import java.util.UUID


@Composable
fun EatPoopYouCatApp(
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            WelcomeScreen(
                onNavigateToSentence = {
                    navController.navigate("sentence/${it}") {
                        popUpTo("home")
                    }
                },
                onNavigateToPreviousGames = {
                    navController.navigate("games")
                })
        }

        composable(
            "sentence/{EntryId}",
            arguments = listOf(
                navArgument("EntryId") { type = NavType.StringType }
            )
        ) {
            SentenceScreen {
                navController.navigate("draw/${it}"){
                    popUpTo("home")
                }
            }
        }
        composable(
            "draw/{EntryId}",
            arguments = listOf(
                navArgument("EntryId") { type = NavType.StringType }
            )
        ) {
            DrawScreen {
                navController.navigate("sentence/${it}"){
                    popUpTo("home")
                }
            }
        }
        composable(
            "games",
        ) {
            PreviousGamesScreen (
                onGameClick = { navController.navigate("game/${it}") }
            )
        }
        composable(
            "game/{GameId}",
            arguments = listOf(
                navArgument("GameId") { type = NavType.StringType }
            )
        ) {
            PreviousGameScreen()
        }
    }
}

@Composable
fun Spinner(
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Center,
            modifier = Modifier
                .size(100.dp)
                .background(White, shape = RoundedCornerShape(8.dp))
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: GreetingViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
    onNavigateToPreviousGames: () -> Unit,
) {
    EatPoopYouCatTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (viewModel.isLoading)
                Spinner()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.welcome_message),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                if (0 == 1) {
                    OutlinedTextField(
                        value = viewModel.userName,
                        onValueChange = {
                            viewModel.updateNickName(it)
                        },
                        modifier = modifier,
                        enabled = true,
                        readOnly = false,
                        shape = RoundedCornerShape(8.dp),

                        label = { Text(stringResource(R.string.nickname), modifier = modifier) },
                    )
                }
                StartGame(onNavigateToSentence)
                ViewPreviousGames(onNavigateToPreviousGames)
            }
        }
    }
}

@Composable
fun ViewPreviousGames(navTo: () -> Unit) {
    Button(onClick = {
        navTo()
    }) {
        Text(stringResource(id = R.string.previous_games))
    }
}

@Composable
fun StartGame(
    onNavigateToSentence: (String) -> Unit,
    greetingViewModel: GreetingViewModel = hiltViewModel()
) {
    Button(onClick = {
        val entryId = UUID.randomUUID()
        greetingViewModel.saveNewGame(
            entryId
        ) { onNavigateToSentence(entryId.toString()) }
    }) {
        Text(stringResource(id = R.string.dialog_start_game))
    }
}
