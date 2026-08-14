package dev.develsinthedetails.eatpoopyoucat.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.develsinthedetails.eatpoopyoucat.data.local.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Entry is the main unit of game data it holds the drawing/sentence
 * and metadata like playerId, gameId, timePassed
 */
@Entity(
    foreignKeys = [ForeignKey
        (
        entity = Game::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = CASCADE
    ), ForeignKey
        (
        entity = Player::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = CASCADE
    )],
    indices = [Index("gameId"), Index("playerId")],
)

//@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Entry(
    @PrimaryKey val id: Uuid,
    val playerId: Uuid,
    val localPlayerName: String? = null,
    val sequence: Int,
    val gameId: Uuid,
    val timePassed: Int,
    val sentence: String? = null,
    val drawing: ByteArray? = null,
    @Serializable(InstantSerializer::class)
    val createdAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry

        if (id != other.id) return false
        return gameId == other.gameId
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + gameId.hashCode()
        return result
    }
}

enum class EntryType {
    Unknown,
    First,
    Sentence,
    Drawing
}


val Entry.type: EntryType
    get() {
        if (this.sequence == 0 && this.sentence == null)
            return EntryType.First
        if (this.sentence != null)
            return EntryType.Sentence
        if (this.drawing != null)
            return EntryType.Drawing
        return EntryType.Unknown
    }