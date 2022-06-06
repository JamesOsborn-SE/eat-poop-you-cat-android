package com.example.eatpoopyoucat.data
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    foreignKeys = [ ForeignKey
        (entity = Game::class,
        parentColumns = ["uid"],
        childColumns = ["gameId"],
        onDelete = CASCADE
    )],
    indices = [Index("gameId")]
)
data class Entry(
    @PrimaryKey val uid: UUID,
    val Sequence: Int,
    val Sentence: String?,
    val Image: ByteArray?,
    //val player: Player,
    val gameId: UUID,
    val timePassed: Int,


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry

        if (uid != other.uid) return false
        if (gameId != other.gameId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + gameId.hashCode()
        return result
    }
}
