package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class Client(
    private val serverUrl: Url,
    private val appSettings: AppSettings
) {
    // find build/dist/wasmJs/productionExecutable -type f \( -name "*.wasm" -o -name "*.js" -o -name "*.css" -o -name "*.htm*" -o -name "*.svg" \) -exec gzip -9 -k {} \;
    // find . -type f \( -name "*.wasm" -o -name "*.js" -o -name "*.css" -o -name "*.htm*" -o -name "*.svg" -o -name "*.xml" -o -name "*.cvr" \) -exec gzip -9 {} \;
    // todo CD wipe pngs in ./file for smaller file sizes
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    private val httpClient = HttpClient {
        install(WebSockets) {
            pingInterval = 15.seconds
        }
    }
    private val pendingMutex = Mutex()
    private val pendingRequests = mutableMapOf<String, CompletableDeferred<GameEvent>>()

    private val sessionMutex = Mutex()
    private val sendMutex = Mutex()

    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null

    private val incomingEvents = Channel<GameEvent>(
        capacity = Channel.BUFFERED
    )

    @Volatile
    private var closed = false

    /**
     * Server-pushed events, such as AskTakeTurn and broadcast TakeTurn events.
     */
    val events: Flow<GameEvent> = incomingEvents.receiveAsFlow()

    init {
        connectionJob = scope.launch {
            maintainConnection()
        }
    }

    private suspend fun maintainConnection() {
        var retryDelay = 500L

        while (scope.isActive && !closed) {
            try {
                val connection = openSession()
                registerPlayerId(appSettings.playerId)
                retryDelay = 500L
                readIncomingFrames(connection)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("WebSocket connection error: ${e.message}")
            } finally {
                clearSession()
            }

            if (scope.isActive && !closed) {
                delay(retryDelay.milliseconds)
                retryDelay = (retryDelay * 2).coerceAtMost(60_000L)
            }
        }
    }

    private suspend fun openSession(): DefaultClientWebSocketSession {
        return sessionMutex.withLock {
            val existing = session

            if (existing != null && existing.isActive) {
                return@withLock existing
            }

            val newSession = httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = serverUrl.host,
                port = serverUrl.port,
                path = serverUrl.encodedPath.ifEmpty { "/ws" }
            )

            session = newSession
            newSession
        }
    }

    private suspend fun getSession(): DefaultClientWebSocketSession {
        while (scope.isActive && !closed) {
            val existing = sessionMutex.withLock {
                session?.takeIf { it.isActive }
            }

            if (existing != null) {
                return existing
            }

            delay(100.milliseconds)
        }

        throw CancellationException("Client is closed")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun readIncomingFrames(
        connection: DefaultClientWebSocketSession,
    ) {
        try {
            for (frame in connection.incoming) {
                if (frame !is Frame.Binary) {
                    continue
                }

                val event = Cbor.decodeFromByteArray<GameEvent>(
                    frame.readBytes()
                )

                val requestId = event.requestId

                if (requestId != null) {
                    val pending = pendingMutex.withLock {
                        pendingRequests[requestId]
                    }

                    if (pending != null) {
                        pending.complete(event)
                    } else {
                        println("Received response for unknown requestId=$requestId")
                    }
                } else {
                    incomingEvents.send(event)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("WebSocket reader error: ${e.message}")
        } finally {
            cancelPendingRequests()
        }
    }

    private suspend fun cancelPendingRequests() {
        val failedRequests = pendingMutex.withLock {
            pendingRequests.values.toList().also {
                pendingRequests.clear()
            }
        }

        failedRequests.forEach { request ->
            request.cancel(
                CancellationException(
                    "WebSocket disconnected from $serverUrl"
                )
            )
        }
    }

    private suspend fun clearSession() {
        val connection = sessionMutex.withLock {
            session.also {
                session = null
            }
        }

        runCatching {
            connection?.close()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun send(
        connection: DefaultClientWebSocketSession,
        event: GameEvent,
    ) {
        val bytes = Cbor.encodeToByteArray<GameEvent>(event)

        sendMutex.withLock {
            connection.send(
                Frame.Binary(
                    fin = true,
                    data = bytes,
                )
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T : GameEvent> rpc(
        request: GameEvent,
    ): T? {
        val requestId = Uuid.random().toString()
        val response = CompletableDeferred<GameEvent>()

        pendingMutex.withLock {
            pendingRequests[requestId] = response
        }

        try {
            val requestWithId = request.withRequestId(requestId)

            val received = withTimeoutOrNull(15.seconds) {
                val connection = getSession()

                try {
                    send(connection, requestWithId)
                    response.await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("RPC send error: ${e.message}")
                    null
                }
            } ?: return null

            return when (received) {
                is T -> received

                is GameEvent.Success
                    if (T::class == GameEvent.Success::class) -> {
                    @Suppress("UNCHECKED_CAST")
                    received as T
                }

                is GameEvent.Error -> {
                    println("Server returned error: ${received.message}")
                    null
                }

                else -> {
                    println(
                        "Unexpected response type: " +
                                received::class.simpleName
                    )
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("RPC error: ${e.message}")
            return null
        } finally {
            pendingMutex.withLock {
                pendingRequests.remove(requestId)
            }
        }
    }

    suspend fun getGame(gameId: Uuid): GameWithRosters? {
        val response = rpc<GameEvent.ResponseGameWithRosters>(
            GameEvent.RequestGameWithRosters(gameId)
        )

        return response?.game
    }

    suspend fun joinGame(player: Roster): Boolean {
        return rpc<GameEvent.Success>(
            GameEvent.JoinGame(player)
        ) != null
    }

    suspend fun registerPlayerId(playerId: Uuid): Boolean {
        return rpc<GameEvent.Success>(
            GameEvent.RegisterPlayerId(
                playerId = playerId
            )
        ) != null
    }

    suspend fun turnComplete(newEntry: Entry): Boolean {
        return rpc<GameEvent.Success>(
            GameEvent.TurnComplete(
                entry = newEntry,
                gameId = newEntry.gameId,
            )
        ) != null
    }

    suspend fun updateGame(game: GameWithEntries): List<Entry> {
        val knownSequences = game.entries.map { it.sequence }

        val response = rpc<GameEvent.ResponseMissingEntries>(
            GameEvent.RequestMissingEntries(
                gameId = game.game.id,
                knownTurns = knownSequences,
            )
        )

        return response?.entries.orEmpty()
    }

    suspend fun close() {
        if (closed) {
            return
        }

        closed = true
        connectionJob?.cancel()

        clearSession()

        pendingMutex.withLock {
            pendingRequests.values.forEach { request ->
                request.cancel(
                    CancellationException("Client closed")
                )
            }

            pendingRequests.clear()
        }
    }

    fun destroy() {
        closed = true
        scope.cancel()
        httpClient.close()
    }
}

private fun GameEvent.withRequestId(
    requestId: String,
): GameEvent {
    return when (this) {
        is GameEvent.RequestGameWithRosters ->
            copy(requestId = requestId)

        is GameEvent.JoinGame ->
            copy(requestId = requestId)

        is GameEvent.TakeYourTurn ->
            copy(requestId = requestId)

        is GameEvent.RequestMissingEntries ->
            copy(requestId = requestId)

        is GameEvent.ResponseGameWithRosters ->
            copy(requestId = requestId)

        is GameEvent.ResponseMissingEntries ->
            copy(requestId = requestId)

        is GameEvent.Success ->
            copy(requestId = requestId)

        is GameEvent.Error ->
            copy(requestId = requestId)

        else -> this
    }
}
