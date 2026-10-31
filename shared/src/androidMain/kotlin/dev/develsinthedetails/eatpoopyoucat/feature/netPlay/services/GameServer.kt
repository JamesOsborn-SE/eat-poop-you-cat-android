package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.app.SystemNotifyEvent
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_DRAW_URI
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_SENTENCE_URI
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.provideSystemNotification
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.latestEntry
import dev.develsinthedetails.eatpoopyoucat.data.models.nextSequence
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.no
import eatpoopyoucat.shared.generated.resources.yes
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
import korlibs.math.isOdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import org.jetbrains.compose.resources.getString
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
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

                // Start new monitors
                for (game in pendingGames) {
                    if (!activeMonitors.containsKey(game.id)) {
                        activeMonitors[game.id] = manageTurns(game.id)
                    }
                }

                // Clean up obsolete monitors
                activeMonitors.keys().toList().forEach { gameId ->
                    if (gameId !in pendingGameIds) {
                        activeMonitors.remove(gameId)?.cancel()
                    }
                }

            }
    }

    private fun manageTurns(gameId: Uuid): Job =
        serverScope!!.launch {
            var backoff = 1_000L
            val maxBackoff = 30_000L
            while (isActive) {
                try {
                    GameTurnMonitor(gameId, targetPlayers = 3).run()
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("Monitor failed for game $gameId: ${e.message}")
                    delay(backoff.milliseconds)
                    backoff = (backoff * 2).coerceAtMost(maxBackoff)
                }
            }
        }

    private inner class GameTurnMonitor(
        private val gameId: Uuid,
        private val targetPlayers: Int,
    ) {
        private var timerJob: Job? = null
        private var activeTurnId: Uuid? = null
        private var pendingTurnPlayerId: Uuid? = null
        private var lastHandledSequence: Int = -5

        suspend fun run() = coroutineScope {
            val events = Channel<MonitorEvent>(Channel.BUFFERED)

            val stateJob = launch {
                combine(
                    repository.getNetGameFlow(gameId),
                    connectionManager.connectedPlayers, // connected players acts as a trigger but data isn't directly used
                ) { netGame, _ ->
                    MonitorEvent.StateChanged(netGame)
                }.collect { events.send(it) }
            }

            try {
                for (event in events) {
                    when (event) {
                        is MonitorEvent.StateChanged -> {
                            val shouldContinue =
                                handleStateChanged(event, events, this)

                            if (!shouldContinue) break
                        }

                        is MonitorEvent.CancelTurn -> {
                            handleCancelTurn(event)
                        }
                    }
                }
            } catch (e: CancellationException) {
                println("DEBUG: Game $gameId: Cancelled. Exception: ${e.message}")

                throw e
            } finally {
                stateJob.cancel()
                timerJob?.cancel()
                events.close()
                activeMonitors.remove(gameId)
            }
        }

        /**
         * Returns false when monitoring should stop, e.g. when the game is complete.
         */
        private suspend fun handleStateChanged(
            event: MonitorEvent.StateChanged,
            events: SendChannel<MonitorEvent>,
            scope: CoroutineScope,
        ): Boolean {
            // if the game is over, stop monitoring and send broadcast.
            if (event.netGame.game.turns != null) {
                connectionManager.broadcast(GameEvent.GameComplete(event.netGame.game))
                printDebugLine(gameId, "Game is over. Stopping monitor.")
                repository.updateActiveTurn(gameId, null)
                activeMonitors.remove(gameId)
                return false
            }

            val currentSequence = event.netGame.entries.nextSequence

            if (currentSequence != lastHandledSequence) {
                printDebugLine(gameId, "New turn: $currentSequence")
                pendingTurnPlayerId = null
                lastHandledSequence = currentSequence
                activeTurnId = Uuid.random()
            }

            if (pendingTurnPlayerId != null) return true

            val availablePlayers =
                availablePlayers(appSettings.playerId, event.netGame, connectionManager)

            // if some players dropped connection after the game started don't punish remaining players.
            val hasEnoughPlayers =
                availablePlayers.isNotEmpty() && (availablePlayers.size + currentSequence) >= targetPlayers

            val canAssignTurn = hasEnoughPlayers && activeTurnId != null
            if (canAssignTurn) {
                pendingTurnPlayerId = availablePlayers.random().playerId

                updateTimeout(
                    scope,
                    events,
                    event.netGame.game.activeTurnId,
                    event.netGame.game.timeout
                )

                assignTurn(event, currentSequence)
            }
            return true
        }

        private suspend fun handleCancelTurn(
            event: MonitorEvent.CancelTurn,
        ) {
            if (event.turnId == activeTurnId) {
                connectionManager.sendToPlayer(
                    playerId = event.playerId,
                    event = GameEvent.TooSlow(gameId, event.turnId)
                )
                pendingTurnPlayerId = null
                repository.updateActiveTurn(gameId, null)
            }
        }

        private suspend fun updateTimeout(
            scope: CoroutineScope,
            events: SendChannel<MonitorEvent>,
            gameActiveTurnId: Uuid?,
            timeout: Int?,
        ) {
            if (gameActiveTurnId != activeTurnId) {
                timerJob?.cancel()
                val timeout = timeout?.minutes ?: 666666.minutes
                val playerId = pendingTurnPlayerId
                val turnId = activeTurnId

                if (playerId == null || turnId == null) return

                timerJob = scope.launch {
                    delay(timeout)
                    events.send(
                        MonitorEvent.CancelTurn(
                            playerId,
                            turnId
                        )
                    )
                }
                repository.updateActiveTurn(gameId, activeTurnId)
            }
        }

        private suspend fun assignTurn(
            event: MonitorEvent.StateChanged,
            sequence: Int,
        ) {
            println("assignTurn: $sequence, playerId=$pendingTurnPlayerId, activeTurnId=$activeTurnId")
            val activeTurnId = activeTurnId ?: return
            val pendingTurnPlayerId = pendingTurnPlayerId ?: return
            if (pendingTurnPlayerId != appSettings.playerId) {
                val latestEntry = event.netGame.entries.latestEntry
                connectionManager.sendToPlayer(
                    playerId = pendingTurnPlayerId,
                    event = GameEvent.TakeYourTurn(
                        latestEntry,
                        activeTurnId,
                        gameId
                    )
                )
            } else {
                val navTo = suspend {
                    if (sequence.isOdd) {
                        navigationManager.navigate(
                            NavigationCommand.OpenDraw(gameId, GameMode.LAN)
                        )
                    } else {
                        navigationManager.navigate(
                            NavigationCommand.OpenSentence(gameId, GameMode.LAN)
                        )
                    }
                }
                notifier.show(
                    id = activeTurnId,
                    actionLabel = "Take You Turn?",
                    onAction = navTo,
                    message = "It's your turn to play"
                )
                if (provideSystemNotification()) {
                    val destUri =
                        (if (sequence.isOdd) DEEPLINK_DRAW_URI else DEEPLINK_SENTENCE_URI) + "/$gameId/${event.netGame.game.gameMode}"
                    notifier.showSystemNotification(
                        SystemNotifyEvent(
                            title = "It's your turn to play",
                            text = "Take You Turn?",
                            yes = getString(Res.string.yes),
                            no = getString(Res.string.no),
                            destUri = destUri
                        )
                    )
                }
            }
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
    data class StateChanged(val netGame: NetGame) : MonitorEvent
    data class CancelTurn(val playerId: Uuid, val turnId: Uuid) : MonitorEvent
}
