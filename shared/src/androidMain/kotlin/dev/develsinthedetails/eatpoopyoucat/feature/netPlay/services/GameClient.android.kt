package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.http.Url
import kotlin.uuid.Uuid

actual class GameClient actual constructor(
    val serverUrl: Url,
    val repository: AppRepository,
    val appSettings: AppSettings,
) {
    private val connectionManager = WebSocketConnectionManager(repository)

    // todo remove this hack should be moved into handleStateChanged in game server
    actual suspend fun turnComplete(newEntry: Entry): Boolean {
        val ge = GameEvent.TurnComplete(
            entry = newEntry,
            gameId = newEntry.gameId,
        )
        connectionManager.broadcast(ge)
        return true
    }

    actual suspend fun getGame(gameId: Uuid): NetGame? {
        return null
    }

    actual suspend fun joinGame(player: Roster): Boolean {
        return false
    }

    actual suspend fun registerPlayerId(playerId: Uuid): Boolean {
        return false
    }

    actual suspend fun updateGame(game: NetGame): List<Entry> {
        return listOf()
    }

    actual suspend fun ensureGameMatchesServer(gameId: Uuid) {
    }

    actual suspend fun dismissTurn(turnId: Uuid) {
    }
}