package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(indices =  [Index("id")])
data class Player(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "name") val nickname: String,
    val lanAddress: String? = null,
    val torAddress: String? = null,
    val createdAt: Instant? = null,
)