package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.App
import dev.develsinthedetails.eatpoopyoucat.app.Credits
import dev.develsinthedetails.eatpoopyoucat.app.Draw
import dev.develsinthedetails.eatpoopyoucat.app.Home
import dev.develsinthedetails.eatpoopyoucat.app.Import
import dev.develsinthedetails.eatpoopyoucat.app.InProgressGameDetails
import dev.develsinthedetails.eatpoopyoucat.app.InProgressGames
import dev.develsinthedetails.eatpoopyoucat.app.Join
import dev.develsinthedetails.eatpoopyoucat.app.New
import dev.develsinthedetails.eatpoopyoucat.app.PreviousGameDetails
import dev.develsinthedetails.eatpoopyoucat.app.PreviousGames
import dev.develsinthedetails.eatpoopyoucat.app.PrivacyPolicy
import dev.develsinthedetails.eatpoopyoucat.app.Sentence
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin
import kotlin.uuid.Uuid

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    startKoin {
        modules(appModule)
    }
    onWasmReady {
        val body = document.body ?: return@onWasmReady
        ComposeViewport(body) {
            App(onNavHostReady = { navController ->


                val fragment = window.location.hash
                    .substringAfter("#", "")

                when {
                    fragment.startsWith("join_") -> {
                        val gameId = fragment
                            .substringAfter("join_")
                            .let(Uuid::parse)

                        navController.navigate(Join(gameId))
                    }

                    fragment.startsWith("previous_games") -> {
                        navController.navigate(PreviousGames)
                    }

                    fragment.startsWith("credits") -> {
                        navController.navigate(Credits)
                    }
                }
                navController.bindToBrowserNavigation { entry ->
                    val route = entry.destination.route.orEmpty()

                    when {
                        route.startsWith(Home.serializer().descriptor.serialName) -> {
                            "#home"
                        }

                        route.startsWith(PreviousGames.serializer().descriptor.serialName) -> {
                            "#previous_games"
                        }

                        route.startsWith(Credits.serializer().descriptor.serialName) -> {
                            "#credits"
                        }

                        route.startsWith(PrivacyPolicy.serializer().descriptor.serialName) -> {
                            "#privacy_policy"
                        }

                        route.startsWith(New.serializer().descriptor.serialName) -> {
                            "#new"
                        }

                        route.startsWith(PreviousGameDetails.serializer().descriptor.serialName) -> {
                            val args = entry.toRoute<PreviousGameDetails>()
                            "#previous_game_details_${args.gameId}"
                        }

                        route.startsWith(Sentence.serializer().descriptor.serialName) -> {
                            val args = entry.toRoute<Sentence>()
                            "#sentence_${args.gameId}_${args.gameMode}"
                        }

                        route.startsWith(Draw.serializer().descriptor.serialName) -> {
                            val args = entry.toRoute<Draw>()
                            "#draw_${args.gameId}_${args.gameMode}"
                        }

                        route.startsWith(InProgressGames.serializer().descriptor.serialName) -> {
                            "#in_progress_games"
                        }

                        route.startsWith(InProgressGameDetails.serializer().descriptor.serialName) -> {
                            val args = entry.toRoute<InProgressGameDetails>()
                            "#in_progress_game_details_${args.gameId}"
                        }

                        route.startsWith(Import.serializer().descriptor.serialName) -> {
                            "#import"
                        }

                        route.startsWith(Join.serializer().descriptor.serialName) -> {
                            val args = entry.toRoute<Join>()
                            "#join_${args.gameId}"
                        }

                        else -> ""
                    }
                }
            })
        }
    }
}
