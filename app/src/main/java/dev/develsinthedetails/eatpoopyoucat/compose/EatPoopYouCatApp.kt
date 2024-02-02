package dev.develsinthedetails.eatpoopyoucat.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onNavigateToHome = {navController.navigate("home")},
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
