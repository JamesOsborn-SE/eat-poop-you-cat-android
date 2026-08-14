package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.data.local.InstantSerializer
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
    @Serializable(InstantSerializer::class)
    val createdAt: Instant? = null,
    @ColumnInfo(defaultValue = "'LOCAL'")
    val gameMode: GameMode = GameMode.LOCAL //todo remove = GameMode.LOCAL
)
