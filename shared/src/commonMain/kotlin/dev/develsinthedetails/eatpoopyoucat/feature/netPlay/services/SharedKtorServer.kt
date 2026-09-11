package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class SharedKtorServer(
    private val gameRouter: GameRouter,
    private val repository: AppRepository,
    private val client: Client,
    private val appSettings: AppSettings
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null
    private var serverScope: CoroutineScope? = null
    private val activeMonitors = ConcurrentHashMap<Uuid, Job>()

    @OptIn(ExperimentalSerializationApi::class)
    fun start() {
        if (server == null) {
            serverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            server = embeddedServer(Netty, port = 3947, host = "0.0.0.0") {
                install(ContentNegotiation) { cbor() }
                install(Resources)

                routing {
                    with(gameRouter) { gameRoutes() }
                    get("api/endpoints") {
                        val routes = this@routing.getAllRoutes()
                        call.respondText(routes.joinToString("\n"))
                    }
                }
                install(StatusPages) {
                    status(HttpStatusCode.NotFound) { call, status ->
                        call.respondText(text = "404: Page Not Found", status = status)
                    }
                }
            }.start(wait = false)
            serverScope?.launch {
                observePendingGames()
            }
        }
    }

    fun stop() {
        serverScope?.cancel()
        serverScope = null

        server?.stop(1000, 2000)
        server = null
    }

    private fun observePendingGames() {
        serverScope?.launch {
            repository.getActiveHostedGameWithRostersFlow(playerId = appSettings.playerId)
                .collect { pendingGames ->
                    val pendingGameIds = pendingGames.map { it.id }.toSet()

                    val iterator = activeMonitors.entries.iterator()
                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        if (entry.key !in pendingGameIds) {
                            entry.value.cancel()
                            iterator.remove()
                        }
                    }

                    for (game in pendingGames) {
                        if (!activeMonitors.containsKey(game.id)) {
                            monitorGameStart(
                                game.id, timeoutMillis = (game.timeout?.toLong()
                                    ?: (60 * 1)) * 1000L
                            )
                        }
                    }
                }
        }
    }

    private fun monitorGameStart(gameId: Uuid, targetPlayers: Int = 4, timeoutMillis: Long) {
        val job = serverScope?.launch {
            val rosterFlow = repository.getRostersByGameFlow(gameId)

            val triggeredRoster = withTimeoutOrNull(timeoutMillis.milliseconds) {
                rosterFlow.first { roster -> roster.size >= targetPlayers }
            }

            val finalRoster = triggeredRoster ?: rosterFlow.first()

            if (finalRoster.isNotEmpty()) {
                val startingPlayer = finalRoster.random()

                println("Game $gameId started automatically! Turn assigned to: ${startingPlayer.nickname}")

                client.askToTakeTurn(startingPlayer)
            }

            activeMonitors.remove(gameId)
        }
        if (job != null)
            activeMonitors[gameId] = job
    }
}