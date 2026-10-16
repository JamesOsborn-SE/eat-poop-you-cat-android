package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import korlibs.math.isEven
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class GameServer(
    private val gameEventHandler: GameEventHandler,
    private val staticRouter: StaticRouter,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
    private val navigationManager: NavigationManager,
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null
    private var serverScope: CoroutineScope? = null
    private val connectionManager = WebSocketConnectionManager()

    private val activeMonitors = ConcurrentHashMap<Uuid, Job>()

    @OptIn(ExperimentalSerializationApi::class)
    fun start() {
        println("start server")
        if (server != null) return

        serverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        server = embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") {
            install(Compression) {
                gzip {
                    priority = 2.0
                }
                deflate {
                    priority = 10.0
                    minimumSize(1024)
                }
            }
            install(ContentNegotiation) { cbor() }
            install(Resources)
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 30.seconds
                masking = false
            }
            install(CORS) {
                allowHost("localhost:$SERVER_PORT")
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Get)
                allowHeader(io.ktor.http.HttpHeaders.ContentType)
            }
            routing {
                with(staticRouter) { staticRoutes() }

                webSocket("/ws") {
                    connectionManager.addSession(this)
                    try {
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Binary) {
                                launch(Dispatchers.Default) {
                                    val event = Cbor.decodeFromByteArray(
                                        GameEvent.serializer(),
                                        frame.readBytes()
                                    )
                                    gameEventHandler.handleWebSocketEvent(
                                        event = event,
                                        session = this@webSocket,
                                        connectionManager = connectionManager
                                    )
                                }
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        println("WebSocket error: ${e.message}")
                    } finally {
                        connectionManager.removeSession(this)
                    }
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

    fun stop() {
        serverScope?.cancel()
        serverScope = null
        server?.stop(1000, 2000)
        server = null
        activeMonitors.clear()
    }

    private suspend fun observePendingGames() {
        repository.getActiveHostedGameWithRostersFlow(playerId = appSettings.playerId)
            .collect { pendingGames ->
                val pendingGameIds = pendingGames.map { it.id }.toSet()

                // Clean up obsolete monitors
                activeMonitors.keys().toList().forEach { gameId ->
                    if (gameId !in pendingGameIds) {
                        activeMonitors.remove(gameId)?.cancel()
                    }
                }

                // Start new monitors
                for (game in pendingGames) {
                    if (!activeMonitors.containsKey(game.id)) {
                        activeMonitors[game.id] = monitorGame(game.id)
                    }
                }
            }
    }

    private fun monitorGame(
        gameId: Uuid,
        targetPlayers: Int = 4,
    ): Job = serverScope!!.launch {
        try {
            println("connected players: ${connectionManager.connectedPlayers} .")
            println("activeMonitors $activeMonitors")
            val events = Channel<MonitorEvent>(Channel.UNLIMITED)

            launch {
                combine(
                    repository.getNetGameFlow(gameId),
                    connectionManager.connectedPlayers
                ) { netGame, players ->
                    MonitorEvent.StateChanged(netGame, players)
                }.collect(events::send)
            }

            var timerJob: Job? = null
            var activeTurnId: Uuid? = null
            var pendingTurnPlayerId: Uuid? = null
            var lastHandledSequence: Int = -1

            for (event in events) {
                when (event) {
                    is MonitorEvent.StateChanged -> {
                        println("Game $gameId; Start monitor.")
                        if (event.netGame.game.turns != null) {
                            println("Game $gameId is over. Stopping monitor.")
                            timerJob?.cancel()
                            cancel() // Cancels this specific job
                            break
                        }

                        val latestEntry = event.netGame.entries.maxByOrNull { it.sequence }
                        val currentSequence = latestEntry?.sequence ?: 0
                        val isFirstTurn = latestEntry == null

                        if (currentSequence != lastHandledSequence) {
                            pendingTurnPlayerId = null
                            lastHandledSequence = currentSequence
                        }

                        val availablePlayers =
                            availablePlayers(appSettings.playerId, event.netGame, connectionManager)

                        val hasEnoughPlayers = event.netGame.roster.size >= targetPlayers

                        val canStartTurn =
                            pendingTurnPlayerId == null &&
                                    availablePlayers.isNotEmpty() &&
                                    (!isFirstTurn || hasEnoughPlayers)
                        println("canStartTurn=$canStartTurn")
                        if (canStartTurn) {

                            activeTurnId = Uuid.random()
                            pendingTurnPlayerId = availablePlayers.random().playerId
                            println("pendingTurnPlayerId=$pendingTurnPlayerId")
                            println("activeTurnId=$activeTurnId")
                            println("event.netGame.game.activeTurnId=${event.netGame.game.activeTurnId}")
                            if (event.netGame.game.activeTurnId != activeTurnId) {
                                timerJob?.cancel()
                                timerJob = launch {
                                    val timeout =
                                        (event.netGame.game.timeout?.minutes ?: 666666.minutes)
                                    delay(timeout)
                                    events.send(
                                        MonitorEvent.CancelTurn(
                                            pendingTurnPlayerId!!,
                                            activeTurnId
                                        )
                                    )
                                }
                            }

                            println("Game $gameId progress automatically! Turn assigned to player id: $pendingTurnPlayerId")
                            if (pendingTurnPlayerId == appSettings.playerId) {
                                val command = if (currentSequence.isEven) {
                                    NavigationCommand.OpenSentence(
                                        event.netGame.game.id,
                                        GameMode.LAN
                                    )
                                } else {
                                    NavigationCommand.OpenDraw(event.netGame.game.id, GameMode.LAN)
                                }
                                navigationManager.navigate(command)
                            } else {
                                connectionManager.sendToPlayer(
                                    playerId = pendingTurnPlayerId,
                                    event = GameEvent.TakeYourTurn(
                                        latestEntry,
                                        activeTurnId,
                                        gameId
                                    )
                                )
                            }
                            repository.updateActiveTurn(event.netGame.game.id, activeTurnId)
                        }
                    }

                    is MonitorEvent.CancelTurn -> {
                        if (event.turnId == activeTurnId) {
                            connectionManager.sendToPlayer(
                                playerId = event.playerId,
                                event = GameEvent.TooSlow(gameId, activeTurnId)
                            )
                            pendingTurnPlayerId = null
                        }
                    }
                }
            }

        } finally {
            activeMonitors.remove(gameId)
        }
    }

    companion object {
        fun availablePlayers(
            myPlayerId: Uuid,
            netGame: NetGame,
            connectionManager: WebSocketConnectionManager
        ): List<Roster> {
            val alreadyPlayed = netGame.entries.map { it.playerId }.toSet()
            val availablePlayers = netGame.roster.filter { player ->
                val hasNotPlayed = player.playerId !in alreadyPlayed
                val isConnected = connectionManager.hasActiveSession(player.playerId)
                val isHost = player.playerId == myPlayerId
                hasNotPlayed && (isConnected || isHost)
            }
            return availablePlayers

        }
    }
}

sealed interface MonitorEvent {
    data class StateChanged(val netGame: NetGame, val players: Set<Uuid>) : MonitorEvent
    data class CancelTurn(val playerId: Uuid, val turnId: Uuid) : MonitorEvent
}