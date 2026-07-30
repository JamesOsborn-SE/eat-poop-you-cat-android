package dev.develsinthedetails.eatpoopyoucat.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Date
import java.util.UUID

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
        playerDao.insert(player.copy(createdAt = Date(System.currentTimeMillis())))
    }

    suspend fun updatePlayer(player: Player) = playerDao.update(player)
    fun getPlayer(id: UUID): Flow<Player?> = playerDao.get(id)

    // ==========================================
    // Game functions
    // ==========================================
    suspend fun createGame(game: Game) {
        gameDao.insert(game.copy(createdAt = Date(System.currentTimeMillis())))
    }

    suspend fun deleteGame(id: String) = gameDao.delete(UUID.fromString(id))
    suspend fun deleteGame(id: UUID) = gameDao.delete(id)
    fun getAllGamesWithEntries() = gameDao.getAllWithEntries()
    suspend fun getAllGamesWithEntriesAsync() = gameDao.getAllWithEntriesAsync()
    suspend fun getAllGames() = gameDao.getAllAsync()
    fun getGameWithEntries(id: String) = gameDao.getWithEntries(UUID.fromString(id))
    suspend fun getGameWithEntriesAsync(id: UUID) = gameDao.getWithEntriesAsync(id)

    // ==========================================
    // Enrty functions
    // ==========================================
    suspend fun createEntry(entry: Entry) =
        entryDao.insert(entry.copy(createdAt = Date(System.currentTimeMillis())))

    fun getEntry(id: String) = entryDao.get(UUID.fromString(id))
    suspend fun getEntryAsync(id: String) = entryDao.getAsync(UUID.fromString(id))
    suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
    suspend fun getEntriesAsync(gameId: String) =
        entryDao.getAllEntriesByGameAsync(UUID.fromString(gameId))

    // ==========================================
    // Roster functions
    // ==========================================
    fun getAllRosters(): Flow<List<Roster>> = rosterDao.getAll()
    fun getRostersByGame(id: UUID): Flow<List<Roster>> = rosterDao.getAllByGame(id)
    fun getRostersByPlayer(id: UUID): Flow<List<Roster>> = rosterDao.getAllByPlayer(id)
    suspend fun insert(roster: Roster) = rosterDao.insert(roster)
    suspend fun deleteByGame(gameId: UUID) = rosterDao.deleteByGame(gameId)
    suspend fun deletePlayer(playerId: UUID) = rosterDao.deletePlayer(playerId)
    suspend fun delete(gameId: UUID, playerId: UUID) = rosterDao.delete(gameId, playerId)
    suspend fun update(roster: Roster) = rosterDao.update(roster)
    suspend fun deleteAll() = rosterDao.deleteAll()
    fun getRosterHashAndCountFlow(gameId: UUID): Flow<RosterHashAndCount> {
        return rosterDao.getOrderedPlayerIds(gameId).map { playerIds ->
            RosterHashAndCount(
                hash = generateRosterHash(playerIds), count = playerIds.size
            )
        }.distinctUntilChanged()
    }

    private fun generateRosterHash(playerIds: List<UUID>): String {
        if (playerIds.isEmpty()) return ""
        val combinedIds = playerIds.joinToString(separator = "") { it.toString() }
        val bytes = MessageDigest.getInstance("SHA-256").digest(combinedIds.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}