package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface GameDao {

    @Transaction
    @Query("SELECT * FROM game")
    fun getAll(): Flow<List<Game>>

    @Transaction
    @Query("SELECT * FROM game")
    fun getAllWithEntries(): Flow<List<GameWithEntries>>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAllWithEntriesAsync(): List<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAllAsync(): List<Game>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun getWithEntries(id: UUID): Flow<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    suspend fun getWithEntriesAsync(id: UUID): GameWithEntries

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Transaction
    @Query("DELETE FROM Game WHERE id=:id")
    suspend fun delete(id: UUID)

    @Transaction
    @Query("DELETE FROM Game")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)
}
