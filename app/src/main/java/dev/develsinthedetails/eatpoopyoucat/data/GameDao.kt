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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Transaction
    @Query("DELETE FROM Game WHERE id=:id")
    suspend fun delete(id: UUID)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)


}
