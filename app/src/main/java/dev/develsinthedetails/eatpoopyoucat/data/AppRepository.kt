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
    fun getPlayer(id: String): Flow<Player?> = playerDao.get(UUID.fromString(id))

    suspend fun createGame(game: Game) = gameDao.insert(game)
    fun getGame(id: UUID) = gameDao.get(id)
    fun getGame(id: String) = gameDao.get(UUID.fromString(id))
    fun getAllGames() = gameDao.getAll()
    fun getAllGamesWithEntries() = gameDao.getAllWithEntries()
    fun getGameWithEntries(id: String) = gameDao.getWithEntries(UUID.fromString(id))
    fun getGameWithEntries(id: UUID) = gameDao.getWithEntries(id)


    suspend fun createEntry(entry: Entry) = entryDao.insert(entry)
    fun getEntry(id: UUID): Flow<Entry> = entryDao.get(id)
    fun getEntry(id: String) = entryDao.get(UUID.fromString(id))
    suspend fun getAllEntries(): List<Entry> = entryDao.getAll()
    fun getAllEntriesByGame(id: UUID)= entryDao.getAllEntriesByGame(id)
    fun getAllEntriesByGame(id: String)= entryDao.getAllEntriesByGame(UUID.fromString(id))
    suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
}
