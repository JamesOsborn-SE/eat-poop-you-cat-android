package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface EntryDao {

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    fun getFlow(id: Uuid): Flow<Entry?>

    @Transaction
    @Query("SELECT * FROM entry")
    suspend fun getAll(): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    suspend fun get(id: Uuid): Entry?

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:id")
    fun getAllEntriesByGameFlow(id: Uuid): Flow<List<Entry>>

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:gameId")
    suspend fun getAllEntriesByGame(gameId: Uuid): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:gameId AND sequence not in (:knownTurns)")
    suspend fun getMissingEntries(gameId: Uuid, knownTurns: List<Int>): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id")
    suspend fun getEntryAndPlayers(id: Uuid): Entry

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Transaction
    @Query("DELETE FROM entry")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Entry>)

    @Transaction
    @Upsert
    suspend fun upsert(entry: Entry)

    @Transaction
    @Query("SELECT gameId FROM entry where id=:id")
    suspend fun getGameId(id: Uuid): Uuid

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:gameId ORDER BY sequence DESC LIMIT 1")
    suspend fun getLast(gameId: Uuid): Entry?
}