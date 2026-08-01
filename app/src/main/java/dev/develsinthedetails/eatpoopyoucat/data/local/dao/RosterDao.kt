package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface RosterDao {
    @Query("SELECT * FROM roster")
    fun getAll(): Flow<List<Roster>>

    @Query("SELECT * FROM roster WHERE gameId=:id")
    fun getAllByGame(id: Uuid): Flow<List<Roster>>

    @Query("SELECT playerId FROM roster WHERE gameId=:gameId ORDER BY playerId ASC")
    fun getOrderedPlayerIds(gameId: Uuid): Flow<List<Uuid>>

    @Query("SELECT * FROM roster WHERE playerId=:id")
    fun getAllByPlayer(id: Uuid): Flow<List<Roster>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(roster: Roster)

    @Query("DELETE FROM roster WHERE gameId=:gameId")
    suspend fun deleteByGame(gameId: Uuid)

    @Query("DELETE FROM roster WHERE playerId=:playerId")
    suspend fun deletePlayer(playerId: Uuid)

    @Query("DELETE FROM roster WHERE gameId=:gameId AND playerId=:playerId")
    suspend fun delete(gameId: Uuid, playerId: Uuid)

    @Update
    suspend fun update(roster: Roster)

    @Query("DELETE FROM roster")
    suspend fun deleteAll()
}