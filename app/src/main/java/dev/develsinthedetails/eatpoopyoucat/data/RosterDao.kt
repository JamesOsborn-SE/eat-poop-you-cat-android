package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RosterDao {
    @Query("SELECT * FROM roster")
    fun getAll(): Flow<List<Roster>>

    @Query("SELECT * FROM roster WHERE gameId=:id")
    fun getAllByGame(id: UUID): Flow<List<Roster>>

    @Query("SELECT playerId FROM roster WHERE gameId=:gameId ORDER BY playerId ASC")
    fun getOrderedPlayerIds(gameId: UUID): Flow<List<UUID>>

    @Query("SELECT * FROM roster WHERE playerId=:id")
    fun getAllByPlayer(id: UUID): Flow<List<Roster>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(roster: Roster)

    @Query("DELETE FROM roster WHERE gameId=:gameId")
    suspend fun deleteByGame(gameId: UUID)

    @Query("DELETE FROM roster WHERE playerId=:playerId")
    suspend fun deletePlayer(playerId: UUID)

    @Query("DELETE FROM roster WHERE gameId=:gameId AND playerId=:playerId")
    suspend fun delete(gameId: UUID, playerId: UUID)

    @Update
    suspend fun update(roster: Roster)

    @Query("DELETE FROM roster")
    suspend fun deleteAll()
}