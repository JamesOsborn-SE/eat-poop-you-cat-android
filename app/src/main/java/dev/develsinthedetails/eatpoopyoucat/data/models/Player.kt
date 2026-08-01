package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(indices =  [Index("id")])
data class Player(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "name") val name: String,
    val lanAddress: String? = null,
    val torAddress: String? = null,
    val createdAt: Instant? = null,
)