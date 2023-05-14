package dev.develsinthedetails.eatpoopyoucat.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.compose.home.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.SentenceScreen
import kotlin.math.floor

@Composable
fun EatPoopYouCatApp(
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
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
            SentenceScreen(
                onNavigateToDraw = {
                    navController.navigate("draw/${it}") {
                        popUpTo("home")
                    }
                },
                onNavigateToEndedGame = {
                    navController.navigate("game/${it}") {
                        popUpTo("home")
                    }
                })
        }
        composable(
            "draw/{EntryId}",
            arguments = listOf(
                navArgument("EntryId") { type = NavType.StringType }
            )
        ) {
            DrawScreen(onNavigateToSentence = {
                navController.navigate("sentence/${it}") {
                    popUpTo("home")
                }
            }, onNavigateToEndedGame = {
                navController.navigate("game/${it}") {
                    popUpTo("home")
                }
            })
        }
        composable(
            "games",
        ) {
            PreviousGamesScreen(
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

@Preview
@Composable
fun PreviewSpinner() {
    Spinner()
}

@Composable
fun Buttons(
    onSubmit: () -> Unit,
    onEnd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            modifier = Modifier
                .padding(top = 15.dp)
                .align(Alignment.End)
                .shadow(15.dp, shape = RoundedCornerShape(50.dp)),
            onClick = { onSubmit() }
        ) {
            Text(stringResource(R.string.submit))
        }

        Button(
            modifier = Modifier.padding(top = 15.dp)
                .align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondaryContainer),
            onClick = onEnd
        )
        {
            Text(stringResource(R.string.end_game_for_all))
        }
    }
}

@Composable
fun getFill(): Modifier {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val fill = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            val screenMax = floor(screenHeight * .8).dp
            Modifier
                .width(screenMax)
                .height(screenMax)
        }

        else -> {
            Modifier.fillMaxWidth()
        }
    }
    return fill
}

@Preview
@Composable
fun ButtonsPreview() {
    Buttons(onSubmit = {}, onEnd = {})
}