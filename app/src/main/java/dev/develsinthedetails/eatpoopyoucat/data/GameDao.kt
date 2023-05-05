package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface GameDao {

    @Transaction
    @Query("SELECT * FROM game")
    fun getAll(): Flow<List<Game>>

    @Transaction
    @Query("SELECT * FROM game")
    fun getAllWithEntries(): Flow<List<GameWithEntries>>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun getWithEntries(id: UUID): Flow<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game WHERE id=:id")
    fun get(id: UUID): Game

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertWait(game: Game)

    @Transaction
    @Query("DELETE FROM Game")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)


}
