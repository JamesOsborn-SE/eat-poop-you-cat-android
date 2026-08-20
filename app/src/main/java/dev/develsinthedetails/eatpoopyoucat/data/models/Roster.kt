package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import dev.develsinthedetails.eatpoopyoucat.data.local.UuidSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [ForeignKey
        (
        entity = Game::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = CASCADE
    ), ForeignKey
        (
        entity = Player::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = CASCADE
    )],
    indices = [Index("playerId")]
)

@Serializable
data class Roster(
    @Serializable(with = UuidSerializer::class)
    val gameId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val playerId: Uuid,
    val nickname: String,
    val address: String,
    val sequence: Int?,
    val isLeader: Boolean,
    val lastSeen: Instant,
)

data class RosterHashAndCount(
    val hash: String,
    val count: Int
)