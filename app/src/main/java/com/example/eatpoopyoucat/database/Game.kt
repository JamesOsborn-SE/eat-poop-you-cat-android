package com.example.eatpoopyoucat.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Game(
    @PrimaryKey val uid: UUID,
    val Timeout: Int?,
    val Turns: Int?,
    val Entries: Array<Entry>
)
