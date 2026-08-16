package dev.develsinthedetails.eatpoopyoucat.data

import android.net.Uri
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.RosterHashAndCount
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AppRepository(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val entryDao: EntryDao,
    private val rosterDao: RosterDao
) {
    // ==========================================
    // Player functions
    // ==========================================
    suspend fun createPlayer(player: Player) {
        playerDao.insert(player.copy(createdAt = Clock.System.now()))
    }

    suspend fun updatePlayer(player: Player) = playerDao.update(player)
    fun getPlayer(id: Uuid): Flow<Player?> = playerDao.get(id)

    // ==========================================
    // Game functions
    // ==========================================
    suspend fun createGame(game: Game) {
        gameDao.insert(game.copy(createdAt = Clock.System.now()))
    }

    fun getGameFlow(id: Uuid) = gameDao.getFlow(id)
    suspend fun getGame(id: Uuid) = gameDao.get(id)
    suspend fun deleteGame(id: Uuid) = gameDao.delete(id)
    fun getAllGamesWithEntries() = gameDao.getAllWithEntries()
    fun getInProgressGamesWithRosters(): Flow<List<GameWithRosters>> =
        gameDao.getInProgressGamesWithRosters()

    fun getGameWithRosters(id: Uuid): GameWithRosters? = gameDao.getGameWithRosters(id)
    suspend fun getAllGames() = gameDao.getAllAsync()
    fun getGameWithEntries(id: Uuid) = gameDao.getWithEntries(id)
    suspend fun getGameWithEntriesAsync(id: Uuid) = gameDao.getWithEntriesAsync(id)

    suspend fun updateGame(game: Game) = gameDao.updateGame(game)

    // ==========================================
    // Entry functions
    // ==========================================
    suspend fun createEntry(entry: Entry) =
        entryDao.insert(entry.copy(createdAt = Clock.System.now()))

    fun getEntry(id: Uuid) = entryDao.get(id)
    suspend fun getEntryAsync(id: Uuid) = entryDao.getAsync(id)
    suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
    suspend fun getEntriesAsync(gameId: Uuid) =
        entryDao.getAllEntriesByGameAsync(gameId)

    suspend fun getMissingEntriesAsync(gameId: Uuid, knownTurns: List<Int>) =
        entryDao.getMissingEntriesAsync(gameId, knownTurns)

    // ==========================================
    // Roster functions
    // ==========================================
    fun getAllRosters(): Flow<List<Roster>> = rosterDao.getAll()
    fun getRostersByGame(id: Uuid): List<Roster> = rosterDao.getAllByGame(id)
    fun getRostersByGameFlow(id: Uuid): Flow<List<Roster>> = rosterDao.getAllByGameFlow(id)
    fun getLeaderByGame(id: Uuid): Flow<Roster> = rosterDao.getLeaderByGame(id)
    fun getRostersByPlayer(id: Uuid): Flow<List<Roster>> = rosterDao.getAllByPlayer(id)
    fun addPlayer(roster: Roster) = rosterDao.insert(roster)
    suspend fun deleteByGame(gameId: Uuid) = rosterDao.deleteByGame(gameId)
    suspend fun deletePlayer(playerId: Uuid) = rosterDao.deletePlayer(playerId)
    suspend fun delete(gameId: Uuid, playerId: Uuid) = rosterDao.delete(gameId, playerId)
    suspend fun updateRoster(roster: Roster) = rosterDao.update(roster)
    suspend fun deleteAll() = rosterDao.deleteAll()
    suspend fun updateRosterPing(address: Uri, gameId: Uuid, time: Instant) =
        rosterDao.updateRosterPing(address, gameId, time)

    fun getRosterHashAndCount(gameId: Uuid): RosterHashAndCount {
        val playerIds = rosterDao.getOrderedPlayerIds(gameId)
        return RosterHashAndCount(generateRosterHash(playerIds), playerIds.size)
    }

    companion object {
        fun generateRosterHash(playerIds: List<Uuid>): String {
            if (playerIds.isEmpty()) return ""
            val combinedIds = playerIds.joinToString(separator = "") { it.toString() }
            val bytes = MessageDigest.getInstance("SHA-256").digest(combinedIds.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}