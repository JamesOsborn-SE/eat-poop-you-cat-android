package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.develsinthedetails.eatpoopyoucat.data.local.UuidSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
@Entity(indices = [Index("id")])
data class Game(
    @Serializable(with = UuidSerializer::class)
    @PrimaryKey val id: Uuid,
    val timeout: Int?,
    // used to mark game complete so others know if they are missing turns
    val turns: Int?,
    val createdAt: Instant? = null,
)
