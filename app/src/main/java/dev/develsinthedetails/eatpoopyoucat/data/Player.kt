package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Entity(indices =  [Index("id")])
data class Player(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "name") val name: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date? = null,
)