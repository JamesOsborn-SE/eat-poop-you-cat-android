@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.type
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
import io.ktor.server.plugins.compression.zstd.zstd
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

actual class SharedKtorServer actual constructor(
    private val gameServerRouter: GameServerRouter,
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
    actual fun start() {
        if (server == null) {
            serverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            server = embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") {
                install(Compression) {
                    zstd(level = 3) {
                        priority = 1.0
                        minimumSize(1024)
                    }
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
                            coroutineScope {
                                // Server -> client
                                launch {
                                    connectionManager.messages.collect { message ->
                                        connectionManager.sendMessage(this@webSocket, message)
                                    }
                                }

                                // Client -> server
                                launch {
                                    incoming.consumeEach { frame ->
                                        if (frame is Frame.Binary) {
                                            val event = Cbor.decodeFromByteArray(
                                                GameEvent.serializer(),
                                                frame.readBytes()
                                            )

                                            gameServerRouter.handleWebSocketEvent(
                                                event = event,
                                                session = this@webSocket,
                                                connectionManager = connectionManager
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            println("WebSocket error: ${e.message}")
                        } finally {
                            connectionManager.removeSession(this)
                            println("WebSocket client disconnected.")
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
    }

    actual fun stop() {
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

    private fun monitorGameStart(
        gameId: Uuid,
        targetPlayers: Int = 4,
        timeoutMillis: Long
    ) {
        if (activeMonitors.containsKey(gameId)) return

        val job = serverScope?.launch {
            try {
                var pendingTurnPlayerId: Uuid? = null
                var lastHandledSequence: Int = -1

                combine(
                    repository.getNetGameFlow(gameId),
                    connectionManager.connectedPlayers
                ) { netGame, connectedPlayers ->
                    Pair(netGame, connectedPlayers)
                }.collect { (netGame, connectedPlayers) ->

                    if (netGame.game.turns != null) {
                        println("Game $gameId is over. Stopping monitor.")
                        cancel()
                        return@collect
                    }

                    val latestEntry = netGame.entries.maxByOrNull { it.sequence }
                    val currentSequence = latestEntry?.sequence ?: 0
                    if (currentSequence != lastHandledSequence) {
                        pendingTurnPlayerId = null
                        lastHandledSequence = currentSequence
                    }

                    if (pendingTurnPlayerId != null &&
                        pendingTurnPlayerId != appSettings.playerId &&
                        !connectedPlayers.contains(pendingTurnPlayerId)
                    ) {
                        println("Player $pendingTurnPlayerId disconnected. Reassigning turn.")
                        pendingTurnPlayerId = null
                    }

                    val alreadyPlayed = netGame.entries.map { it.playerId }.toSet()

                    val availablePlayers = netGame.roster.filter { player ->
                        val hasNotPlayed = !alreadyPlayed.contains(player.playerId)
                        val isConnected = connectionManager.hasActiveSession(player.playerId)
                        println("DEBUG: hasNotPlayed=$hasNotPlayed")
                        println("DEBUG: isConnected=$isConnected")
                        println("DEBUG: isHost=${player.playerId == appSettings.playerId}")
                        val isHost = player.playerId == appSettings.playerId

                        hasNotPlayed && (isConnected || isHost)
                    }

                    val isFirstTurn = latestEntry == null
                    val hasEnoughPlayers =
                        netGame.roster.size >= targetPlayers || netGame.roster.size > 2

                    if (pendingTurnPlayerId == null && availablePlayers.isNotEmpty() && (!isFirstTurn || hasEnoughPlayers)) {

                        val nextPlayer = availablePlayers.random()
                        pendingTurnPlayerId = nextPlayer.playerId
                        println("Game $gameId progress automatically! Turn assigned to: ${nextPlayer.nickname}")

                        if (nextPlayer.playerId == appSettings.playerId) {
                            if (latestEntry?.type == EntryType.Sentence) {
                                navigationManager.navigate(
                                    NavigationCommand.OpenDraw(
                                        netGame.game.id,
                                        GameMode.LAN
                                    )
                                )
                            } else {
                                navigationManager.navigate(
                                    NavigationCommand.OpenSentence(
                                        netGame.game.id,
                                        GameMode.LAN
                                    )
                                )
                            }
                        }
                        connectionManager.sendToPlayer(
                            playerId = nextPlayer.playerId,
                            event = GameEvent.TakeYourTurn(latestEntry, gameId)
                        )

                    }
                }
            } finally {
                activeMonitors.remove(gameId)
            }
        }

        if (job != null) {
            activeMonitors[gameId] = job
        }
    }

    actual suspend fun sendGameComplete(game: Game) {
        connectionManager.broadcast(GameEvent.GameComplete(game, requestId = null))
    }
}


//todo move to own file

sealed interface ServerMessage {
    data class Broadcast(val event: GameEvent) : ServerMessage

    data class ToPlayer(
        val playerId: Uuid,
        val event: GameEvent
    ) : ServerMessage
}

class WebSocketConnectionManager {
    private val _messages = MutableSharedFlow<ServerMessage>(
        replay = 0,
        extraBufferCapacity = 64
    )

    private val _connectedPlayers = MutableStateFlow<Set<Uuid>>(emptySet())
    val connectedPlayers: StateFlow<Set<Uuid>> = _connectedPlayers.asStateFlow()

    val messages: SharedFlow<ServerMessage> = _messages.asSharedFlow()

    private val sessionsByPlayer =
        ConcurrentHashMap<Uuid, DefaultWebSocketServerSession>()

    private val playerBySession =
        ConcurrentHashMap<DefaultWebSocketServerSession, Uuid>()

    private val sendMutexes =
        ConcurrentHashMap<DefaultWebSocketServerSession, Mutex>()

    fun addSession(session: DefaultWebSocketServerSession) {
        println("WebSocket client connected.")
        sendMutexes[session] = Mutex()
    }

    fun hasActiveSession(playerId: Uuid): Boolean {
        return sessionsByPlayer.containsKey(playerId)
    }

    fun removeSession(session: DefaultWebSocketServerSession) {
        val playerId = playerBySession.remove(session)
        println("WebSocket client disconnected.")
        if (playerId != null) {
            println("DEBUG: Removing player $playerId")
            sessionsByPlayer.remove(playerId, session)
            _connectedPlayers.update { it - playerId }
        }

        sendMutexes.remove(session)
    }

    fun associatePlayer(
        playerId: Uuid,
        session: DefaultWebSocketServerSession
    ) {
        println("Associate player $playerId with session $session")
        sessionsByPlayer[playerId] = session
        playerBySession[session] = playerId
        _connectedPlayers.update { it + playerId }
    }

    // todo use as check for playerId?
    fun playerIdFor(
        session: DefaultWebSocketServerSession
    ): Uuid? = playerBySession[session]

    suspend fun broadcast(event: GameEvent) {
        _messages.emit(ServerMessage.Broadcast(event))
    }

    suspend fun sendToPlayer(
        playerId: Uuid,
        event: GameEvent
    ) {
        _messages.emit(ServerMessage.ToPlayer(playerId, event))
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun send(
        session: DefaultWebSocketServerSession,
        event: GameEvent
    ) {
        val bytes = Cbor.encodeToByteArray(
            GameEvent.serializer(),
            event
        )

        sendMutexes[session]?.withLock {
            session.send(Frame.Binary(fin = true, data = bytes))
        }
    }

    suspend fun sendMessage(
        session: DefaultWebSocketServerSession,
        message: ServerMessage
    ) {
        when (message) {
            is ServerMessage.Broadcast -> {
                send(session, message.event)
            }

            is ServerMessage.ToPlayer -> {
                val session = sessionsByPlayer[message.playerId]
                if (session != null) {
                    send(session, message.event)
                } else {
                    println("No Active Sessions for player")
                }
            }
        }
    }
}