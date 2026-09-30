package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.ForeignKey.Companion.CASCADE
import androidx.room3.Index
import dev.develsinthedetails.eatpoopyoucat.data.local.UuidSerializer
import kotlinx.serialization.Serializable
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
    val isLeader: Boolean = false,

    )
