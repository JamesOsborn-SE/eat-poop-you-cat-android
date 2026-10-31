@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.serialization.ExperimentalSerializationApi

class GameEventHandler(
    private val repository: AppRepository,
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
            // happens when WS connection is established
            // sends back games player is in
            is GameEvent.RegisterPlayerId -> {
                println("DEBUG: RegisterPlayerId=${event.playerId}")
                connectionManager.associatePlayer(
                    playerId = event.playerId,
                    session = session
                )
                reply(
                    GameEvent.Success(
                        "Successfully joined",
                        requestId = event.requestId,
                        event.gameId
                    )
                )
                val gameIds = repository.getGamesForPlayer(event.playerId)
                connectionManager.sendToPlayer(
                    event.playerId,
                    GameEvent.GamesPlayerIsIn(
                        event.playerId,
                        gameIds,
                        requestId = null,
                        event.gameId
                    )
                )
            }

            is GameEvent.JoinGame -> {
                try {
                    repository.addPlayer(event.player)

                    connectionManager.associatePlayer(
                        playerId = event.player.playerId,
                        session = session
                    )

                    reply(
                        GameEvent.Success(
                            "Successfully joined",
                            requestId = event.requestId,
                            event.gameId
                        )
                    )
                } catch (e: Exception) {
                    reply(
                        GameEvent.Error(
                            "Could not join game: ${e.message}",
                            requestId = event.requestId,
                            event.gameId
                        )
                    )
                }
                // Sync users with latest user added
                connectionManager.broadcast(event)
            }

            is GameEvent.TurnComplete -> {
                if (event.entry == null) {
                    reply(
                        GameEvent.Error(
                            "Entry cannot be null",
                            requestId = event.requestId,
                            event.gameId
                        )
                    )
                    return
                }
                repository.createEntry(event.entry)
                reply(GameEvent.Success("Turn saved", requestId = event.requestId, event.gameId))

                // Sync users with latest turn
                connectionManager.broadcast(event)
            }

            is GameEvent.RequestNetGame -> {
                val game = repository.getNetGame(event.gameId)
                reply(
                    GameEvent.ResponseNetGame(
                        game,
                        requestId = event.requestId,
                        event.gameId
                    )
                )
            }

            is GameEvent.RequestMissingEntries -> {
                val missing = repository.getMissingEntries(
                    event.gameId,
                    event.knownTurns
                )

                reply(
                    GameEvent.ResponseMissingEntries(
                        missing,
                        requestId = event.requestId,
                        event.gameId
                    )
                )
            }

            is GameEvent.TooSlow -> {
                repository.updateActiveTurn(event.gameId, null)
            }

            else -> Unit
        }
    }
}
