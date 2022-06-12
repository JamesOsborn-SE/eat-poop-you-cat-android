package dev.develsinthedetails.eatpoopyoucat.data
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    foreignKeys = [ ForeignKey
        (entity = Game::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = CASCADE
    ),ForeignKey
        (entity = Player::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = CASCADE
    )],
    indices = [Index("gameId"), Index("playerId")]
)
data class Entry(
    @PrimaryKey val id: UUID,
    val playerId: UUID,
    val sequence: Int,
    val sentence: String?,
    val image: ByteArray?,
    val gameId: UUID,
    val timePassed: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry

        if (id != other.id) return false
        if (gameId != other.gameId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + gameId.hashCode()
        return result
    }
}
