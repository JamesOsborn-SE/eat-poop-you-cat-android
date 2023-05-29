package dev.develsinthedetails.eatpoopyoucat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color.Companion.White
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
                onGoHome = { navController.navigate("home") },
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
