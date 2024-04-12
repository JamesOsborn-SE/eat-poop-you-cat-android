package dev.develsinthedetails.eatpoopyoucat.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val entryDao: EntryDao
) {
    suspend fun createPlayer(player: Player) = playerDao.insert(player)
    suspend fun updatePlayer(player: Player) = playerDao.update(player)
    fun getPlayer(id: UUID): Flow<Player?> = playerDao.get(id)
    suspend fun createGame(game: Game) = gameDao.insert(game)
    suspend fun deleteGame(id: String) = gameDao.delete(UUID.fromString(id))
    suspend fun deleteGame(id: UUID) = gameDao.delete(id)
    fun getAllGamesWithEntries() = gameDao.getAllWithEntries()
    suspend fun getAllGames() = gameDao.getAllAsync()
    fun getGameWithEntries(id: String) = gameDao.getWithEntries(UUID.fromString(id))
    fun getGameWithEntries(id: UUID) = gameDao.getWithEntries(id)
    suspend fun getGameWithEntriesAsync(id: UUID) = gameDao.getWithEntriesAsync(id)


    suspend fun createEntry(entry: Entry) = entryDao.insert(entry)
    fun getEntry(id: String) = entryDao.get(UUID.fromString(id))
    suspend fun getEntryAsync(id: String) = entryDao.getAsync(UUID.fromString(id))
    suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
    suspend fun getEntriesAsync(gameId: String) = entryDao.getAllEntriesByGameAsync(UUID.fromString(gameId))
    suspend fun getGameWithEntriesByEntry(entryId: String): GameWithEntries {
        val gameId = entryDao.getAsync(UUID.fromString(entryId)).gameId
        return gameDao.getWithEntriesAsync(gameId)
    }
}
