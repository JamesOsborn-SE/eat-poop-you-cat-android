package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
class WebSocketConnectionManager {
    private val _connectedPlayers = MutableStateFlow<Set<Uuid>>(emptySet())
    val connectedPlayers: StateFlow<Set<Uuid>> = _connectedPlayers.asStateFlow()

    private class ClientConnection(val session: DefaultWebSocketServerSession) {
        val mutex = Mutex()
        suspend fun send(bytes: ByteArray) {
            mutex.withLock {
                session.send(Frame.Binary(fin = true, data = bytes))
            }
        }
    }

    private val activeConnections =
        ConcurrentHashMap<DefaultWebSocketServerSession, ClientConnection>()
    private val sessionsByPlayer = ConcurrentHashMap<Uuid, ClientConnection>()
    private val playerBySession = ConcurrentHashMap<DefaultWebSocketServerSession, Uuid>()

    fun addSession(session: DefaultWebSocketServerSession) {
        activeConnections[session] = ClientConnection(session)
        println("WebSocket client connected.")
    }

    fun removeSession(session: DefaultWebSocketServerSession) {
        activeConnections.remove(session)
        val playerId = playerBySession.remove(session)

        if (playerId != null) {
            sessionsByPlayer.remove(playerId)
            _connectedPlayers.update { it - playerId }
            println("Player $playerId disconnected.")
        }
    }

    fun associatePlayer(playerId: Uuid, session: DefaultWebSocketServerSession) {
        val connection = activeConnections[session] ?: return
        sessionsByPlayer[playerId] = connection
        playerBySession[session] = playerId
        _connectedPlayers.update { it + playerId }
        println("Associated player $playerId with session.")
    }

    fun hasActiveSession(playerId: Uuid): Boolean = sessionsByPlayer.containsKey(playerId)

    suspend fun broadcast(event: GameEvent) {
        // Encode once, send to many (Better performance)
        val bytes = Cbor.encodeToByteArray(GameEvent.serializer(), event.withoutRequestId())
        sessionsByPlayer.values.forEach { connection ->
            try {
                connection.send(bytes)
            } catch (e: Exception) { /* Ignore dropped clients */
            }
        }
    }

    suspend fun sendToPlayer(playerId: Uuid, event: GameEvent) {
        val connection = sessionsByPlayer[playerId] ?: return
        val bytes = Cbor.encodeToByteArray(GameEvent.serializer(), event.withoutRequestId())
        try {
            connection.send(bytes)
        } catch (e: Exception) {
            println("Failed to send to player $playerId: ${e.message}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun send(session: DefaultWebSocketServerSession, event: GameEvent) {
        val connection = activeConnections[session] ?: return

        val bytes = Cbor.encodeToByteArray(GameEvent.serializer(), event)
        try {
            connection.send(bytes)
        } catch (e: Exception) {
            println("Failed to send directly to session: ${e.message}")
        }
    }
}