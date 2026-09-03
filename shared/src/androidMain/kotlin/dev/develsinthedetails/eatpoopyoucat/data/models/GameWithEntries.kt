package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room3.Embedded
import androidx.room3.Relation
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import kotlinx.serialization.Serializable

@Serializable
data class GameWithEntries(
    @Embedded
    var game: Game,
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var entries: List<Entry> = emptyList()
)

fun GameWithEntries.entriesAreValid(): Boolean {
    return this.entries.isNotEmpty() && this.entries.all{ it.sentence.isNullOrBlank().xor(it.drawing == null) }
}

@Serializable
data class GameWithRosters(
    @Embedded
    var game: Game,
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var roster: List<Roster> = emptyList()
)

fun GameWithRosters.hash(): String{
    val sorted = this.roster.map { it.playerId }.sortedBy { it.toString() }
    return AppRepository.generateRosterHash(sorted)
}