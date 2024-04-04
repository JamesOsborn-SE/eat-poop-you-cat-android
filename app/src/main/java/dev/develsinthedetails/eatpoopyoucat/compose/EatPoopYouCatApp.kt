package dev.develsinthedetails.eatpoopyoucat.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
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
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.type
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.ID
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen

@Composable
fun EatPoopYouCatApp(
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSentence = {
                    navController.navigate(Screen.Sentence.byId(it)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToPreviousGames = {
                    navController.navigate(Screen.Games.route)
                },
                onNavigateToCredits = {
                    navController.navigate(Screen.Credits.route)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                })
        }

        composable(
            Screen.Sentence.route,
            arguments = listOf(
                navArgument(ID) { type = NavType.StringType }
            )
        ) {
            SentenceScreen(
                onNavigateToDraw = {
                    navController.navigate(Screen.Draw.byId(it)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToEndedGame = {
                    navController.navigate(Screen.Game.byId(it)) {
                        popUpTo(Screen.Home.route)
                    }
                })
        }
        composable(
            Screen.Draw.route,
            arguments = listOf(
                navArgument(ID) { type = NavType.StringType }
            )
        ) {
            DrawScreen(onNavigateToSentence = {
                navController.navigate(Screen.Sentence.byId(it)) {
                    popUpTo(Screen.Home.route)
                }
            }, onNavigateToEndedGame = {
                navController.navigate(Screen.Game.byId(it)) {
                    popUpTo(Screen.Home.route)
                }
            })
        }
        composable(
            Screen.Games.route,
        ) {
            PreviousGamesScreen(
                onGoHome = { navController.navigate(Screen.Home.route) },
                onGameClick = { navController.navigate(Screen.Game.byId(it)) }
            )
        }
        composable(
            Screen.Game.route,
            arguments = listOf(
                navArgument(ID) { type = NavType.StringType }
            )
        ) {
            PreviousGameScreen(continueGame = navigateToNextTurn(navController))
        }
        composable(Screen.Credits.route) {
            CreditsScreen()
        }
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen()
        }
    }
}

@Composable
private fun navigateToNextTurn(navController: NavHostController): (Entry) -> Unit =
    {
        if (it.type == EntryType.Drawing)
            navController.navigate(Screen.Sentence.byId(it.id.toString()))
        else if (it.type == EntryType.Sentence)
            navController.navigate(Screen.Draw.byId(it.id.toString()))
    }

