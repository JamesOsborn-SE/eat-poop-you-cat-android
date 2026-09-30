@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.serialization.ExperimentalSerializationApi

actual class GameServerRouter actual constructor(
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleWebSocketEvent(
        event: GameEvent,
        session: DefaultWebSocketServerSession,
        connectionManager: WebSocketConnectionManager
    ) {
        suspend fun reply(event: GameEvent) {
            connectionManager.send(session, event)
        }

        when (event) {
            is GameEvent.JoinGame -> {
                try {
                    repository.addPlayer(event.player)

                    connectionManager.associatePlayer(
                        playerId = event.player.playerId,
                        session = session
                    )

                    reply(GameEvent.Success("Successfully joined", requestId = event.requestId))
                } catch (e: Exception) {
                    reply(
                        GameEvent.Error(
                            "Could not join game: ${e.message}", requestId = event.requestId
                        )
                    )
                }
                connectionManager.broadcast(event.copy(requestId = null))
            }

            is GameEvent.TurnComplete -> {
                if (event.entry == null) {
                    reply(GameEvent.Error("Entry cannot be null", requestId = event.requestId))
                    return
                }
                repository.upsertEntry(event.entry)
                reply(GameEvent.Success("Turn saved", requestId = event.requestId))

                // Sync users with latest turn
                connectionManager.broadcast(event.copy(requestId = null))
            }

            is GameEvent.TakeYourTurn -> {
                if (event.entry == null) {
                    reply(GameEvent.Error("Entry cannot be null", requestId = event.requestId))
                    return
                }
                repository.upsertEntry(event.entry)
                reply(GameEvent.Success("Turn saved", requestId = event.requestId))

                // Sync users with latest turn
                connectionManager.broadcast(event.copy(requestId = null))
            }

            is GameEvent.RequestGameWithRosters -> {
                val game = repository.getGameWithRosters(event.gameId)
                reply(GameEvent.ResponseGameWithRosters(game, requestId = event.requestId))
            }

            is GameEvent.RequestMissingEntries -> {
                val missing = repository.getMissingEntries(
                    event.gameId,
                    event.knownTurns
                )

                reply(GameEvent.ResponseMissingEntries(missing, requestId = event.requestId))
            }

            is GameEvent.RegisterPlayerId -> {
                println("DEBUG: RegisterPlayerId=${event.playerId}")
                connectionManager.associatePlayer(
                    playerId = event.playerId,
                    session = session
                )
                reply(GameEvent.Success("Successfully joined", requestId = event.requestId))
            }

            else -> Unit
        }
    }
}
