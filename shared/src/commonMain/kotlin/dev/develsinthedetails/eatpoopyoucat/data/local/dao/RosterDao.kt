package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
interface RosterDao {
    @Query("SELECT * FROM roster")
    suspend fun getAll(): List<Roster>

    @Query("SELECT * FROM roster WHERE gameId=:id")
    suspend fun getAllByGame(id: Uuid): List<Roster>


    @Query("SELECT * FROM roster WHERE gameId=:id")
    fun getAllByGameFlow(id: Uuid): Flow<List<Roster>>

    @Query("SELECT * FROM roster WHERE gameId=:id and isLeader=1 LIMIT 1")
    fun getLeaderByGameFlow(id: Uuid): Flow<Roster>

    @Query("SELECT playerId FROM roster WHERE gameId=:gameId ORDER BY playerId ASC")
    suspend fun getOrderedPlayerIds(gameId: Uuid): List<Uuid>

    @Query("SELECT * FROM roster WHERE playerId=:id")
    fun getAllByPlayerFlow(id: Uuid): Flow<List<Roster>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(roster: Roster)

    @Query("DELETE FROM roster WHERE gameId=:gameId")
    suspend fun deleteByGame(gameId: Uuid)

    @Query("DELETE FROM roster WHERE playerId=:playerId")
    suspend fun deletePlayer(playerId: Uuid)

    @Query("DELETE FROM roster WHERE gameId=:gameId AND playerId=:playerId")
    suspend fun delete(gameId: Uuid, playerId: Uuid)

    @Upsert
    suspend fun upsert(roster: Roster)

    @Query(
        """
        UPDATE Roster 
        SET lastSeen = :time 
        WHERE address = :address AND gameId = :gameId
    """
    )
    suspend fun updateRosterPing(address: String, gameId: Uuid, time: Instant)

    @Query("DELETE FROM roster")
    suspend fun deleteAll()

    @Transaction
    @Upsert
    suspend fun upsert(rosters: List<Roster>)
}