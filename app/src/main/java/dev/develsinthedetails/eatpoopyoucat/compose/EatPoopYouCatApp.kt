package dev.develsinthedetails.eatpoopyoucat.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import dev.develsinthedetails.eatpoopyoucat.ImportGamesActivity
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.compose.home.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.type
import dev.develsinthedetails.eatpoopyoucat.utilities.ID
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen
import dev.develsinthedetails.eatpoopyoucat.utilities.saveGames
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGamesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EatPoopYouCatApp(
    goto: String?,
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
                onGameClick = { navController.navigate(Screen.Game.byId(it)) },
                onImportGames = onImportGames(context = context),
                onBackupGames = onBackupGames(coroutineScope, games, context)
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
    if(goto != null)
        navController.navigate(goto)
}

@Composable
private fun onImportGames(context: Context): ManagedActivityResultLauncher<String, Uri?> {
    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { importFileUri ->
        if (importFileUri != null) {
            val intent = Intent(context, ImportGamesActivity::class.java)
            intent.data = importFileUri
            context.startActivity(intent)
        }
    }
    return pickPictureLauncher
}

@Composable
private fun onBackupGames(
    coroutineScope: CoroutineScope,
    games: List<GameWithEntries>?,
    context: Context
): () -> Unit = {
    coroutineScope.launch {
        if (games?.isNotEmpty() == true) {
            Toast.makeText(
                context,
                "saving...",
                Toast.LENGTH_LONG
            ).show()
            val filePath = saveGames(context, games!!)
            Toast.makeText(
                context,
                "saved to: $filePath",
                Toast.LENGTH_LONG,

                ).show()
        } else {
            Toast.makeText(
                context,
                "you has no games to save bruh",
                Toast.LENGTH_LONG
            ).show()
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

