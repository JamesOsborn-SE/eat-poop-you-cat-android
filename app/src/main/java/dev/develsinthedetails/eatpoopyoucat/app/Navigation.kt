package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.utilities.saveGames
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesActivity
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.LanGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.CreditsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NicknameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.PrivacyPolicyScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val UuidNavType = object : NavType<Uuid>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Uuid? {
        return bundle.getString(key)?.let { Uuid.parse(it) }
    }

    override fun parseValue(value: String): Uuid {
        return Uuid.parse(value)
    }

    override fun put(bundle: Bundle, key: String, value: Uuid) {
        bundle.putString(key, value.toString())
    }
}

//sealed interface Screen {
@Serializable
data object Home

@Serializable
data object ImportPreviousGames

@Serializable
data object PreviousGames

@Serializable
data object Credits

@Serializable
data object PrivacyPolicy

@Serializable
data class NewGame(val id: Uuid)

@Serializable
data class LanGame(val id: Uuid)

@Serializable
data class PreviousGame(val gameId: Uuid)

@Serializable
data class Sentence(val id: Uuid)

@Serializable
data class Draw(val id: Uuid)
//}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun EatPoopYouCatApp() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                onNavigateToNewGame = {
                    navController.navigate(NewGame(it)) {
                        popUpTo<Home>()
                    }
                },
                onNavigateToPreviousGames = {
                    navController.navigate(PreviousGames) {
                        popUpTo<Home>()
                    }
                },
                onNavigateToCredits = {
                    navController.navigate(Credits)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(PrivacyPolicy)
                }
            )
        }

        composable<NewGame>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType),
        ) {
            NicknameScreen(nav = navController)
        }

        composable<LanGame>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
        ) {
            LanGameScreen(nav = navController)
        }

        composable<Sentence>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
        ) {
            SentenceScreen(
                onNavigateToDraw = { id ->
                    navController.navigate(NewGame(id)) {
                        popUpTo<Home>()
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Home)
                },
                onNavigateToEndedGame = { id ->
                    navController.navigate(PreviousGame(id)) {
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<Draw>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
        ) {
            DrawScreen(
                onNavigateToSentence = { id ->
                    navController.navigate(NewGame(id)) {
                        popUpTo<Home>()
                    }
                },
                onNavigateToEndedGame = { id ->
                    navController.navigate(PreviousGame(id)) {
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<PreviousGames>(
            deepLinks = listOf(
                navDeepLink<PreviousGames>(basePath = "epyc://previous_games")
            )
        ) {
            PreviousGamesScreen(
                onGoHome = {
                    navController.navigate(Home) {
                        popUpTo<Home>()
                    }
                },
                onGameClick = { id ->
                    navController.navigate(PreviousGame(id))
                },
                onBackupGames = onBackupGames(coroutineScope, context),
                onImportGames = onImportGames()
            )
        }

        composable<PreviousGame>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
        ) {
            PreviousGameScreen(
                onContinueGame = navigateToNextNickName(navController),
                onBackupGame = onBackupGames(coroutineScope = coroutineScope, context = context),
                onImportGames = onImportGames(),
                onBack = {
                    navController.navigate(PreviousGames) {
                        popUpTo<PreviousGames>()
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<Credits> {
            CreditsScreen(SharedPref.playerId().toString()) {
                navController.navigate(Home) {
                    popUpTo<Home>()
                }
            }
        }

        composable<PrivacyPolicy> {
            PrivacyPolicyScreen {
                navController.navigate(Home) {
                    popUpTo<Home>()
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
            navController.navigate(Sentence(it.id)) // 'it.id' automatically maps to the Uuid parameter
        else if (it.type == EntryType.Sentence)
            navController.navigate(Draw(it.id))
    }

@OptIn(ExperimentalUuidApi::class)
@Composable
fun navigateToNextNickName(navController: NavHostController): (Uuid) -> Unit =
    {
        navController.navigate(NewGame(it)) {
            popUpTo<Home>()
        }
    }