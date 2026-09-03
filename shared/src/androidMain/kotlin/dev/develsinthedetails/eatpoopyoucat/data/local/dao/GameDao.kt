package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface GameDao {

    @Transaction
    @Query("SELECT * FROM game")
    fun getAllFlow(): Flow<List<Game>>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun getFlow(id: Uuid): Flow<Game>

    @Transaction
    @Query("SELECT * FROM game")
    fun getAllWithEntriesFlow(): Flow<List<GameWithEntries>>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAllWithEntries(): List<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAll(): List<Game>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun getWithEntriesFlow(id: Uuid): Flow<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    suspend fun getWithEntries(id: Uuid): GameWithEntries

    @Transaction
    @Query("SELECT * FROM game WHERE turns IS NULL")
    fun getInProgressGamesWithRosters(): Flow<List<GameWithRosters>>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    suspend fun getGameWithRosters(id: Uuid): GameWithRosters?

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Transaction
    @Query("DELETE FROM Game WHERE id=:id")
    suspend fun delete(id: Uuid)

    @Transaction
    @Query("DELETE FROM Game")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)

    @Transaction
    @Upsert
    suspend fun updateGame(game: Game)

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    suspend fun get(id: Uuid): Game
}
