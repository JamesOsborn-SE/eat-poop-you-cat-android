package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
interface RosterDao {
    @Query("SELECT * FROM roster")
    fun getAll(): Flow<List<Roster>>

    @Query("SELECT * FROM roster WHERE gameId=:id")
    fun getAllByGame(id: Uuid): List<Roster>


    @Query("SELECT * FROM roster WHERE gameId=:id")
    fun getAllByGameFlow(id: Uuid): Flow<List<Roster>>

    @Query("SELECT * FROM roster WHERE gameId=:id and isLeader=1 LIMIT 1")
    fun getLeaderByGame(id: Uuid): Flow<Roster>

    @Query("SELECT playerId FROM roster WHERE gameId=:gameId ORDER BY playerId ASC")
    fun getOrderedPlayerIds(gameId: Uuid): List<Uuid>

    @Query("SELECT * FROM roster WHERE playerId=:id")
    fun getAllByPlayer(id: Uuid): Flow<List<Roster>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(roster: Roster)

    @Query("DELETE FROM roster WHERE gameId=:gameId")
    suspend fun deleteByGame(gameId: Uuid)

    @Query("DELETE FROM roster WHERE playerId=:playerId")
    suspend fun deletePlayer(playerId: Uuid)

    @Query("DELETE FROM roster WHERE gameId=:gameId AND playerId=:playerId")
    suspend fun delete(gameId: Uuid, playerId: Uuid)

    @Upsert
    suspend fun update(roster: Roster)

    @Query("""
        UPDATE Roster 
        SET lastSeen = :time 
        WHERE address = :address AND gameId = :gameId
    """)
    suspend fun updateRosterPing(address: Uri, gameId: Uuid, time: Instant)

    @Query("DELETE FROM roster")
    suspend fun deleteAll()
}