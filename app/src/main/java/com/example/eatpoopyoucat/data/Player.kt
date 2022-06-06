package com.example.eatpoopyoucat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Player(
    @PrimaryKey val uid: UUID,
    @ColumnInfo(name = "name") val name: String
)