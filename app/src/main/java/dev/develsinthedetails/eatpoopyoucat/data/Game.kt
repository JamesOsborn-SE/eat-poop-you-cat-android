package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
@Entity(indices = [Index("id")])
data class Game(
    @Serializable(with = UUIDSerializer::class)
    @PrimaryKey val id: UUID,
    val timeout: Int?,
    val turns: Int?,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date? = null,
)
