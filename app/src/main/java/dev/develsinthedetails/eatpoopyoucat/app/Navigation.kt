package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.develsinthedetails.eatpoopyoucat.feature.importgames.ImportGamesActivity
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousgames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousgames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.CreditsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.feature.netplay.LanGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NewGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.PrivacyPolicyScreen
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.core.utilities.saveGames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Credits : Screen
    @Serializable data object PrivacyPolicy : Screen

    @Serializable data class NewGame(val id: Uuid) : Screen
    @Serializable data class LanGame(val id: Uuid) : Screen
    @Serializable data class Game(val id: Uuid) : Screen
    @Serializable data class Sentence(val id: Uuid) : Screen
    @Serializable data class Draw(val id: Uuid) : Screen
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun EatPoopYouCatApp(
    goto: Screen?
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = goto ?: Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToNewGame = {
                    navController.navigate(Screen.NewGame(id = Uuid.random())) {
                        popUpTo<Screen.Home>()
                    }
                },
                onNavigateToPreviousGames = {
                    navController.navigate(Screen.Games) {
                        popUpTo<Screen.Home>()
                    }
                },
                onNavigateToCredits = {
                    navController.navigate(Screen.Credits)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(Screen.PrivacyPolicy)
                }
            )
        }

        composable<Screen.NewGame> { backStackEntry ->
            NewGameScreen(nav = navController)
        }

        composable<Screen.LanGame> {
            LanGameScreen(nav = navController)
        }

        composable<Screen.Sentence> {
            SentenceScreen(
                onNavigateToDraw = { id ->
                    navController.navigate(Screen.NewGame(id)) {
                        popUpTo<Screen.Home>()
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home)
                },
                onNavigateToEndedGame = { id ->
                    navController.navigate(Screen.Game(id)) {
                        popUpTo<Screen.Home>()
                    }
                }
            )
        }

        composable<Screen.Draw> {
            DrawScreen(
                onNavigateToSentence = { id ->
                    navController.navigate(Screen.NewGame(id)) {
                        popUpTo<Screen.Home>()
                    }
                },
                onNavigateToEndedGame = { id ->
                    navController.navigate(Screen.Game(id)) {
                        popUpTo<Screen.Home>()
                    }
                }
            )
        }

        composable<Screen.Games> {
            PreviousGamesScreen(
                onGoHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home>()
                    }
                },
                onGameClick = { id ->
                    navController.navigate(Screen.Game(id))
                },
                onBackupGames = onBackupGames(coroutineScope, context),
                onImportGames = onImportGames()
            )
        }

        composable<Screen.Game> {
            PreviousGameScreen(
                onContinueGame = navigateToNextNickName(navController),
                onBackupGame = onBackupGames(coroutineScope = coroutineScope, context = context),
                onImportGames = onImportGames(),
                onBack = {
                    navController.navigate(Screen.Games) {
                        popUpTo<Screen.Games>()
                        popUpTo<Screen.Home>()
                    }
                }
            )
        }

        composable<Screen.Credits> {
            CreditsScreen(SharedPref.playerId().toString()) {
                navController.navigate(Screen.Home) {
                    popUpTo<Screen.Home>()
                }
            }
        }

        composable<Screen.PrivacyPolicy> {
            PrivacyPolicyScreen {
                navController.navigate(Screen.Home) {
                    popUpTo<Screen.Home>()
                }
            }
        }
    }
}

@Composable
fun onImportGames(): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { importFileUri ->
        importFileUri?.let { uri ->
            val intent = Intent(context, ImportGamesActivity::class.java).apply {
                data = uri
            }
            context.startActivity(intent)
        }
    }
}

@Composable
private fun onBackupGames(
    coroutineScope: CoroutineScope,
    context: Context
): (games: List<GameWithEntries>?) -> Unit = {
    coroutineScope.launch {
        if (it?.isNotEmpty() == true) {
            Toast.makeText(
                context,
                context.getString(R.string.saving),
                Toast.LENGTH_LONG
            ).show()
            val filePath = saveGames(context, it)
            Toast.makeText(
                context,
                context.getString(R.string.saved_to, filePath),
                Toast.LENGTH_LONG,
            ).show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_games_to_save),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun navigateToNextTurn(navController: NavHostController): (Entry) -> Unit =
    {
        if (it.type == EntryType.Drawing || it.type == EntryType.First)
            navController.navigate(Screen.Sentence(it.id)) // 'it.id' automatically maps to the Uuid parameter
        else if (it.type == EntryType.Sentence)
            navController.navigate(Screen.Draw(it.id))
    }

@OptIn(ExperimentalUuidApi::class)
@Composable
fun navigateToNextNickName(navController: NavHostController): (Uuid) -> Unit =
    {
        navController.navigate(Screen.NewGame(it)) {
            popUpTo<Screen.Home>()
        }
    }