package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class GameWithEntries(
    @Embedded
    var game: Game,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    var entries: List<Entry> = emptyList()
)
