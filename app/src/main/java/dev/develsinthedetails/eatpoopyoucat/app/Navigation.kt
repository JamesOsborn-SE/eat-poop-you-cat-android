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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.saveGames
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesActivity
import dev.develsinthedetails.eatpoopyoucat.feature.inProgressGames.InProgressGameDetailsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.inProgressGames.InProgressGames
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.StartNetGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.CreditsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NewGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NicknameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.PrivacyPolicyScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
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

@Serializable
data object Home

@Serializable
data object PreviousGames

@Serializable
data object Credits

@Serializable
data object PrivacyPolicy

@Serializable
data object NewGame

@Serializable
data class Nickname(val previousEntryId: Uuid)

@Serializable
data class PreviousGameDetails(val gameId: Uuid)

@Serializable
data class Sentence(val previousEntryId: Uuid, val nickname: String? = null)

@Serializable
data class Draw(val previousEntryId: Uuid, val nickname: String? = null)


@Serializable
data class StartNetGame(val gameId: Uuid, val gameMode: GameMode)

@Serializable
data object InProgressGames

@Serializable
data class InProgressGameDetails(val gameId: Uuid)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun NavGraph(appSettings: AppSettings = koinInject()) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val useNicknames by appSettings.useNicknamesFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                toNewGame = {
                    navController.navigate(NewGame) {
                        popUpTo<Home>()
                    }
                },
                toPreviousGames = {
                    navController.navigate(PreviousGames) {
                        popUpTo<Home>()
                    }
                },
                toCredits = {
                    navController.navigate(Credits)
                },
                toInProgressGames = {
                    navController.navigate(InProgressGameDetails)
                },
                toPrivacyPolicy = {
                    navController.navigate(PrivacyPolicy)
                }
            )
        }

        composable<NewGame> {
            NewGameScreen(
                onBack = {
                    navController.navigate(Home)
                },
                onNetGame = { gameId: Uuid, gameMode: GameMode ->
                    navController.navigate(StartNetGame(gameId, gameMode))
                },
                onLocal = { previousEntryId: Uuid ->
                    if (useNicknames) {
                        navController.navigate(Nickname(previousEntryId))
                    } else {
                        navController.navigate(Sentence(previousEntryId, null))
                    }
                },
            )
        }

        composable<Sentence>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType),
            deepLinks = listOf(
                navDeepLink<Sentence>(
                    basePath = appSettings.sentenceDeepLink,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                ),
            )
        ) {
            SentenceScreen(
                onNavigateToDraw = { previousEntryId, gameMode ->
                    if (useNicknames && gameMode == GameMode.LOCAL) {
                        navController.navigate(Nickname(previousEntryId)) {
                            popUpTo<Home>()
                        }
                    } else {
                        navController.navigate(
                            Draw(
                                previousEntryId,
                                nickname = null
                            )
                        ) {
                            popUpTo<Home>()
                        }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Home)
                },
                onNavigateToEndedGame = { gameId ->
                    navController.navigate(PreviousGameDetails(gameId)) {
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<Draw>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType),
            deepLinks = listOf(
                navDeepLink<Draw>(
                    basePath = appSettings.drawDeepLink,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                ),
            )
        ) {
            DrawScreen(
                onNavigateToSentence = { previousEntryId, gameMode, gameId ->
                    when {
                        useNicknames && gameMode == GameMode.LOCAL -> {
                            navController.navigate(Nickname(previousEntryId)) {
                                popUpTo<Home>()
                            }
                        }

                        gameMode == GameMode.LOCAL -> {
                            navController.navigate(Sentence(previousEntryId))
                        }

                        else -> {
                            navController.navigate(InProgressGameDetails(gameId = gameId!!))
                        }
                    }
                },
                onNavigateToEndedGame = { gameId ->
                    navController.navigate(PreviousGameDetails(gameId)) {
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<PreviousGames>(
            deepLinks = listOf(
                navDeepLink<PreviousGames>(
                    basePath = appSettings.previousGamesDeepLink,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                )
            )
        ) {
            PreviousGamesScreen(
                onGoHome = {
                    navController.navigate(Home) {
                        popUpTo<Home>()
                    }
                },
                onGameClick = { gameId ->
                    navController.navigate(PreviousGameDetails(gameId))
                },
                onBackupGames = onBackupGames(coroutineScope, context),
                onImportGames = onImportGames()
            )
        }

        composable<PreviousGameDetails>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType),
            deepLinks = listOf(
                navDeepLink<PreviousGameDetails>(
                    basePath = appSettings.previousGameDetailsDeepLink,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                )
            )
        ) {
            PreviousGameScreen(
                onContinueGame = { previousEntryId:Uuid, entryType: EntryType ->
                    if (entryType == EntryType.Sentence){
                        navController.navigate(Draw(previousEntryId))
                    }
                    else{
                        navController.navigate(Sentence(previousEntryId))
                    }
                },
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
            CreditsScreen {
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

        composable<StartNetGame>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) {
            StartNetGameScreen(onBack = {
                navController.navigate(Home)
            }, onStartGame = { gameId: Uuid ->
                navController.navigate(InProgressGameDetails(gameId))
            })
        }

        composable<InProgressGames> {
            InProgressGames(
                onBack = { navController.navigate(Home) },
                toGame = { gameId: Uuid ->
                    navController.navigate(InProgressGameDetails(gameId))
                }
            )
        }

        composable<InProgressGameDetails>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) {
            InProgressGameDetailsScreen(onBack = { navController.navigate(InProgressGames) })
        }

        composable<Nickname>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) {
            NicknameScreen(
                onEnd = { navController.navigate(PreviousGames) },
                onSubmit = { previousEntryId: Uuid, entryType: EntryType, nickname: String ->
                    if (entryType == EntryType.Sentence)
                        navController.navigate(Draw(previousEntryId, nickname))
                    else
                        navController.navigate(Sentence(previousEntryId, nickname))
                }
            )
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
