package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices =  [Index("id")])
data class Player(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "name") val name: String,
)