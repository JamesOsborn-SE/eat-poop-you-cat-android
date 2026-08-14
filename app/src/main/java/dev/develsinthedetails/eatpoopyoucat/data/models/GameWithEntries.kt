package dev.develsinthedetails.eatpoopyoucat.data.models

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

fun GameWithEntries.entriesAreValid(): Boolean {
    return this.entries.all{ it.sentence.isNullOrBlank().xor(it.drawing == null) }
}

@Serializable
data class GameWithRosters(
    @Embedded
    var game: Game,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    var roster: List<Roster> = emptyList()
)