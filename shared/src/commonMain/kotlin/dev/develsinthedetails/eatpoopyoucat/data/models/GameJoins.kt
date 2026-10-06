package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room3.Embedded
import androidx.room3.Relation
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import kotlinx.serialization.Serializable

@Serializable
data class GameWithEntries(
    @Embedded
    var game: Game,
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var entries: List<Entry> = emptyList()
)

fun GameWithEntries.gameIsComplete() =
    (this.game.gameMode == GameMode.LOCAL || (this.game.gameMode != GameMode.LOCAL && this.game.turns != null))
            && this.entries.isNotEmpty()

fun GameWithEntries.gameToCleanUp() = this.game.gameMode == GameMode.LOCAL
        && this.entries.isNotEmpty()
        && this.entries.all { it.sentence.isNullOrBlank().xor(it.drawing == null) }

@Serializable
data class GameWithRosters(
    @Embedded
    var game: Game,
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var roster: List<Roster> = emptyList()
)

@Serializable
data class NetGame(
    @Embedded
    var game: Game,
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var roster: List<Roster> = emptyList(),
    @Relation(parentColumns = ["id"], entityColumns = ["gameId"])
    var entries: List<Entry> = emptyList()
)