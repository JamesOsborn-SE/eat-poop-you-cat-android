package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

/**
 * Entry is the main unit of game data it holds the drawing/sentence
 * and meta data like playerId, gameId, timePassed
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
@Serializable
data class Entry(
    @Serializable(with = UUIDSerializer::class)
    @PrimaryKey val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val playerId: UUID,
    val localPlayerName: String? = null,
    val sequence: Int,
    @Serializable(with = UUIDSerializer::class)
    val gameId: UUID,
    val timePassed: Int,
    val sentence: String? = null,
    val drawing: ByteArray? = null,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date? = null,
){
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


val Entry.type: Any
    get() {
        if (this.sequence == 0 && this.sentence == null)
            return EntryType.First
        if (this.sentence != null)
            return EntryType.Sentence
        if (this.drawing != null)
            return EntryType.Drawing
        return EntryType.Unknown
    }