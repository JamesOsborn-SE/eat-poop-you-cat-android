package dev.develsinthedetails.eatpoopyoucat.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_DRAW_URI
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_PREVIOUS_GAMES_URI
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_PREVIOUS_GAME_DETAILS_URI
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_SENTENCE_URI
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesScreen
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.NetGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.StartNetGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGameDetailsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGames
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameDetailsRoute
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesRoute
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.CreditsScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NewGameScreen
import dev.develsinthedetails.eatpoopyoucat.feature.setup.PrivacyPolicyScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val GameModeType = object : NavType<GameMode>(isNullableAllowed = false) {

    override fun get(bundle: SavedState, key: String): GameMode {
        return bundle.read {
            getString(key).let { GameMode.valueOf(it) }
        }
    }

    override fun parseValue(value: String): GameMode {
        return GameMode.valueOf(value)
    }

    override fun put(bundle: SavedState, key: String, value: GameMode) {
        bundle.write {
            putString(key, value.name)
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
val UuidNavType = object : NavType<Uuid>(isNullableAllowed = false) {

    override fun get(bundle: SavedState, key: String): Uuid {
        return bundle.read {
            getString(key).let { Uuid.parse(it) }
        }
    }

    override fun parseValue(value: String): Uuid {
        return Uuid.parse(value)
    }

    override fun put(bundle: SavedState, key: String, value: Uuid) {
        bundle.write {
            putString(key, value.toString())
        }
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
data class PreviousGameDetails(val gameId: Uuid)

@Serializable
data class Sentence(
    val gameId: Uuid,
    val gameMode: GameMode
)

@Serializable
data class Draw(
    val gameId: Uuid,
    val gameMode: GameMode
)

@Serializable
data class StartNetGame(
    val gameId: Uuid,
    val gameMode: GameMode
)

@Serializable
data object InProgressGames

@Serializable
data class InProgressGameDetails(val gameId: Uuid)

@Serializable
data object ImportGamesRoute

@Serializable
data class NetGameRoute(
    val gameId: Uuid,
    val address: String
)

val appTypeMap = mapOf(
    typeOf<Uuid>() to UuidNavType,
    typeOf<GameMode>() to GameModeType
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun NavGraph(
    netGameParams: Pair<Uuid, String>? = null,
    onNetGameParamsConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()

    LaunchedEffect(netGameParams) {
        if (netGameParams != null) {
            navController.navigate(NetGameRoute(netGameParams.first, netGameParams.second))
            onNetGameParamsConsumed()
        }
    }

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
                    navController.navigate(InProgressGames)
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
                onNewGame = { gameId: Uuid, gameMode: GameMode ->
                    when (gameMode) {
                        GameMode.LOCAL -> {
                            navController.navigate(Sentence(gameId, GameMode.LOCAL))
                        }

                        else -> {
                            navController.navigate(StartNetGame(gameId, gameMode))
                        }
                    }
                },
            )
        }

        composable<Sentence>(
            typeMap = appTypeMap,
            deepLinks = listOf(
                navDeepLink<Sentence>(
                    basePath = DEEPLINK_SENTENCE_URI,
                    typeMap = mapOf(
                        typeOf<Uuid>() to UuidNavType,
                        typeOf<GameMode>() to GameModeType
                    ),
                ),
            )
        ) {
            SentenceScreen(
                toDraw = { gameId, gameMode ->
                    when (gameMode) {
                        GameMode.LOCAL -> {
                            navController.navigate(
                                Draw(
                                    gameId,
                                    gameMode
                                )
                            ) {
                                popUpTo<Home>()
                            }
                        }

                        else -> {
                            navController.navigate(InProgressGameDetails(gameId)) {
                                popUpTo<InProgressGames>()
                            }
                        }
                    }
                },
                toHome = {
                    navController.navigate(Home)
                },
                toEndedGame = { gameId, gameMode ->
                    when (gameMode) {
                        GameMode.LOCAL -> {
                            navController.navigate(PreviousGameDetails(gameId)) {
                                popUpTo<Home>()
                            }
                        }

                        else -> {
                            navController.navigate(InProgressGameDetails(gameId)) {
                                popUpTo<InProgressGames>()
                            }
                        }
                    }
                }
            )
        }

        composable<Draw>(
            typeMap = appTypeMap,
            deepLinks = listOf(
                navDeepLink<Draw>(
                    basePath = DEEPLINK_DRAW_URI,
                    typeMap = mapOf(
                        typeOf<Uuid>() to UuidNavType,
                        typeOf<GameMode>() to GameModeType
                    )
                ),
            )
        ) {
            DrawScreen(
                toSentence = { gameId, gameMode ->
                    when {
                        gameMode == GameMode.LOCAL -> {
                            navController.navigate(Sentence(gameId, gameMode))
                        }

                        else -> {
                            navController.navigate(InProgressGameDetails(gameId = gameId))
                        }
                    }
                },
                toEndedGame = { gameId ->
                    navController.navigate(PreviousGameDetails(gameId)) {
                        popUpTo<PreviousGames>()
                    }
                }
            )
        }

        composable<ImportGamesRoute> {
            ImportGamesScreen(
                finish = {
                    navController.navigate(PreviousGames) {
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<PreviousGames>(
            deepLinks = listOf(
                navDeepLink<PreviousGames>(
                    basePath = DEEPLINK_PREVIOUS_GAMES_URI,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                )
            )
        ) {
            PreviousGamesRoute(
                onGoHome = { navController.navigate(Home) { popUpTo<Home>() } },
                onGameClick = { gameId -> navController.navigate(PreviousGameDetails(gameId)) },
                onNavigateToImport = { navController.navigate(ImportGamesRoute) },
            )
        }

        composable<PreviousGameDetails>(
            typeMap = mapOf(typeOf<Uuid>() to UuidNavType),
            deepLinks = listOf(
                navDeepLink<PreviousGameDetails>(
                    basePath = DEEPLINK_PREVIOUS_GAME_DETAILS_URI,
                    typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
                )
            )
        ) {
            PreviousGameDetailsRoute(
                onContinueGame = { previousEntryId: Uuid, entryType: EntryType ->
                    if (entryType == EntryType.Sentence) {
                        navController.navigate(Draw(previousEntryId, gameMode = GameMode.LOCAL))
                    } else {
                        navController.navigate(Sentence(previousEntryId, gameMode = GameMode.LOCAL))
                    }
                },
                onNavigateToImport = { navController.navigate(ImportGamesRoute) },
                onBack = {
                    navController.navigate(PreviousGames) {
                        popUpTo<PreviousGames>()
                        popUpTo<Home>()
                    }
                }
            )
        }

        composable<NetGameRoute>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val route = backStackEntry.toRoute<NetGameRoute>()
            NetGameScreen(gameId = route.gameId, address = route.address)
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

        composable<StartNetGame>(
            typeMap = appTypeMap
        ) {
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
    }
}