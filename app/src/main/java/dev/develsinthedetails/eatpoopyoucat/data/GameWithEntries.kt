package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Embedded
import androidx.room.Relation
import dev.develsinthedetails.eatpoopyoucat.data.Entry

data class GameWithEntries(
    @Embedded
    var game: Game,
    @Relation(parentColumn = "id", entityColumn="gameId")
    var entries: List<Entry> = emptyList()
)
