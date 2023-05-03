package dev.develsinthedetails.eatpoopyoucat.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    suspend fun createGame(game: Game) {
        gameDao.insert(game);
    }
    fun getGames() = gameDao.getAll()

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: GameRepository? = null

        fun getInstance(gameDao: GameDao) =
            instance ?: synchronized(this) {
                instance ?: GameRepository(gameDao).also { instance = it }
            }
    }
}