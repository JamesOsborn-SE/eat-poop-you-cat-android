package dev.develsinthedetails.eatpoopyoucat.compose

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
import dev.develsinthedetails.eatpoopyoucat.utilities.ID
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen
import dev.develsinthedetails.eatpoopyoucat.utilities.saveGames
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGamesViewModel
import kotlinx.coroutines.launch

@Composable
fun EatPoopYouCatApp(
    viewModel: PreviousGamesViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val games by viewModel.games.observeAsState(initial = null)

    val context = LocalContext.current
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSentence = {
                    navController.navigate(Screen.Sentence.byId(it)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackUpGames = {
                    coroutineScope.launch {
                        if (games?.isNotEmpty() == true) {
                            Toast.makeText(
                                context,
                                "saving...",
                                Toast.LENGTH_LONG
                            ).show()
                            val sgames = saveGames(context, games!!)
                            if (sgames != null)
                            Toast.makeText(
                                    context,
                                    "saved to: ${sgames.path?.replace("/external/","")}",
                                    Toast.LENGTH_LONG
                                ).show()
                            else
                                Toast.makeText(
                                    context,
                                    "share done goofed, you're boned",
                                    Toast.LENGTH_LONG
                                ).show()
                        }
                        else{
                            Toast.makeText(
                                context,
                                "you has no games to save bruh",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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

