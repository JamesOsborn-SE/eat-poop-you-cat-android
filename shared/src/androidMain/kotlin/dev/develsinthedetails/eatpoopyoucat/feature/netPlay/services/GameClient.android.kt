package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.http.Url
import kotlin.uuid.Uuid

actual class GameClient actual constructor(
    val serverUrl: Url,
    val repository: AppRepository,
    val appSettings: AppSettings
) {
    actual suspend fun getGame(gameId: Uuid): GameWithRosters? {
        return null
    }

    actual suspend fun joinGame(player: Roster): Boolean {
        return false
    }

    actual suspend fun registerPlayerId(playerId: Uuid): Boolean {
        return false
    }

    actual suspend fun turnComplete(newEntry: Entry): Boolean {
        return false
    }

    actual suspend fun updateGame(game: GameWithEntries): List<Entry> {
        TODO("Not yet implemented")
    }

    actual suspend fun ensureGameMatchesServer(gameId: Uuid) {
    }
}