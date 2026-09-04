package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player")
    suspend fun getAll(): List<Player>

    @Query("SELECT * FROM player WHERE id=:id")
    fun getFlow(id: Uuid): Flow<Player?>

    @Query("SELECT * FROM player WHERE id=:id")
    suspend fun get(id: Uuid): Player?

    @Query("DELETE FROM player WHERE id=:id")
    suspend fun delete(id: Uuid)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Transaction
    @Upsert
    suspend fun upsert(player: Player)

    @Query("DELETE FROM player")
    suspend fun deleteAll()
}