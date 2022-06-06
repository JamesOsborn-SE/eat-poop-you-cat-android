package com.example.eatpoopyoucat.data

import androidx.room.Embedded
import androidx.room.Relation

class GameWithEntries(
    @Embedded
    var game: Game,
    @Relation(parentColumn = "uid", entityColumn="gameId")
    var entries: List<Entry> = emptyList()
)
