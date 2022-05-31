package com.example.eatpoopyoucat.database
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Entry(
    @PrimaryKey val uid: Int,
    val Sequence: Int,
    val Sentence: String?,
    val Image: ByteArray?,
    val player: Player,
    val timePassed: Int
)
