package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.nextType
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
    private val notifier: Notifier,
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
                maxFrameSize = Long.MAX_VALUE
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
                        activeMonitors[game.id] = manageTurns(game.id)
                    }
                }
            }
    }

    private fun manageTurns(
        gameId: Uuid,
        targetPlayers: Int = 3,
    ): Job = serverScope!!.launch {
        val events = Channel<MonitorEvent>(Channel.BUFFERED)
        var timerJob: Job? = null

        val stateJob = launch {
            combine(
                repository.getNetGameFlow(gameId),
                connectionManager.connectedPlayers
            ) { netGame, players ->
                MonitorEvent.StateChanged(netGame, players)
            }.collect { events.send(it) }
        }

        var activeTurnId: Uuid? = null
        var pendingTurnPlayerId: Uuid? = null
        var lastHandledSequence: Int = -5

        try {
            for (event in events) {
                when (event) {
                    is MonitorEvent.StateChanged -> {
                        if (event.netGame.game.turns != null) {
                            connectionManager.broadcast(GameEvent.GameComplete(event.netGame.game))
                            printDebugLine(gameId, "Game is over. Stopping monitor.")
                            return@launch
                        }
                        val latestEntry = event.netGame.entries.maxByOrNull { it.sequence }
                        val currentSequence =
                            latestEntry?.sequence ?: -1 // HACK: for detecting first turn

                        if (currentSequence != lastHandledSequence) {
                            printDebugLine(gameId, "New turn: $currentSequence")
                            pendingTurnPlayerId = null
                            lastHandledSequence = currentSequence
                            activeTurnId = Uuid.random()
                        }

                        val availablePlayers =
                            availablePlayers(appSettings.playerId, event.netGame, connectionManager)
                        printDebugLine(gameId, "Available players: $availablePlayers")

                        // if some players dropped connection after the game started don't punish remaining players.
                        val hasEnoughPlayers = availablePlayers.isNotEmpty() &&
                                availablePlayers.size + currentSequence + 1 >= targetPlayers
                        printDebugLine(gameId, "hasEnoughPlayers=$hasEnoughPlayers")

                        val canAssignTurn =
                            pendingTurnPlayerId == null &&
                                    hasEnoughPlayers &&
                                    activeTurnId != null

                        printDebugLine(gameId, "canStartTurn=$canAssignTurn")
                        if (!canAssignTurn) continue

                        pendingTurnPlayerId = availablePlayers.random().playerId
                        if (event.netGame.game.activeTurnId != activeTurnId) {
                            timerJob?.cancel()

                            val timeout =
                                (event.netGame.game.timeout?.minutes ?: 666666.minutes)
                            val playerId = pendingTurnPlayerId
                            val turnId = activeTurnId
                            timerJob = launch {
                                delay(timeout)
                                events.send(
                                    MonitorEvent.CancelTurn(
                                        playerId,
                                        turnId
                                    )
                                )
                            }
                            repository.updateActiveTurn(event.netGame.game.id, activeTurnId)
                        }

                        printDebugLine(
                            gameId,
                            "Game progress automatically! Turn assigned to player id: $pendingTurnPlayerId"
                        )
                        if (pendingTurnPlayerId == appSettings.playerId) {
                            val navTo = suspend {
                                val entry = latestEntry!!
                                if (entry.nextType == EntryType.Drawing) {
                                    navigationManager.navigate(
                                        NavigationCommand.OpenDraw(gameId, GameMode.LAN)
                                    )
                                } else {
                                    navigationManager.navigate(
                                        NavigationCommand.OpenSentence(gameId, GameMode.LAN)
                                    )
                                }
                            }
                            val turnId = activeTurnId
                            notifier.show(
                                id = turnId,
                                actionLabel = "Take You Turn?",
                                onAction = navTo,
                                message = "It's your turn to play"
                            )
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
                    }


                    is MonitorEvent.CancelTurn -> {
                        if (event.turnId == activeTurnId) {
                            connectionManager.sendToPlayer(
                                playerId = event.playerId,
                                event = GameEvent.TooSlow(gameId, event.turnId)
                            )
                            pendingTurnPlayerId = null
                        }
                    }
                }
            }

        } finally {
            stateJob.cancel()
            timerJob?.cancel()
            events.close()
            activeMonitors.remove(gameId)
        }
    }

    private fun printDebugLine(gameId: Uuid, s: String) {
        println("DEBUG: Game $gameId: $s")
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