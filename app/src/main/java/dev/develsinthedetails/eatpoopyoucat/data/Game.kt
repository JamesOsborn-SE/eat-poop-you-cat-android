package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices =  [Index("id")])
data class Game(
    @PrimaryKey val id: UUID,
    val timeout: Int?,
    val turns: Int?
)
