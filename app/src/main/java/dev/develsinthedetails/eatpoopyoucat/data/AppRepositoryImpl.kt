package dev.develsinthedetails.eatpoopyoucat.data

import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface IAppRepository {
    val allGames: Flow<List<Game>>
    val allGamesWithEntries: Flow<List<GameWithEntries>>
    val allPlayers: Flow<List<Player>>

    suspend fun createPlayer(player: Player)
    suspend fun createGame(game: Game)
    suspend fun getGame(id: UUID)
    suspend fun createEntry(entry: Entry)
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val entryDao: EntryDao
) : IAppRepository {
    override val allGames: Flow<List<Game>> = gameDao.getAll()
    override val allGamesWithEntries: Flow<List<GameWithEntries>> = gameDao.getAllWithEntries()
    override val allPlayers: Flow<List<Player>> = playerDao.getAll()

    override suspend fun createPlayer(player: Player){
        playerDao.insert(player)
    }
    override suspend fun createGame(game: Game) {
        gameDao.insert(game)
    }

    override suspend fun getGame(id: UUID){
        gameDao.get(id)
    }

    override suspend fun createEntry(entry: Entry){
        entryDao.insert(entry)
    }

    override suspend fun getEntry(id: UUID){
        entryDao.get(id)
    }
}
