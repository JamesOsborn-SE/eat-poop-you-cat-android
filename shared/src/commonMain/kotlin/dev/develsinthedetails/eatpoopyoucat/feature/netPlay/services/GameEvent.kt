package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed class GameEvent {
    abstract val requestId: String?

    @Serializable
    data class RegisterPlayerId(
        val playerId: Uuid,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * Get all the game metadata and player roster for a given game.
     */
    @Serializable
    data class RequestGameWithRosters(
        val gameId: Uuid,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * Returns the game metadata and player roster for a given game.
     */
    @Serializable
    data class ResponseGameWithRosters(
        val game: GameWithRosters?,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * player Joins game reply with ResponseGameWithRosters
     */
    @Serializable
    data class JoinGame(
        val player: Roster,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * Player takes turn
     * Server -> Client
     * Reply with Success if successful
     */
    @Serializable
    data class TakeYourTurn(
        val entry: Entry?,
        val gameId: Uuid,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * Turn is complete
     * update dbs
     * client / server
     */
    @Serializable
    data class TurnComplete(
        val entry: Entry?,
        val gameId: Uuid,
        override val requestId: String? = null
    ) : GameEvent()

    /**
     * Client asks for missing Entries at end of game if missing entries
     * Reply with ResponseMissingEntries
     */
    @Serializable
    data class RequestMissingEntries(
        val gameId: Uuid, val knownTurns: List<Int>,
        override val requestId: String? = null
    ) : GameEvent()

    @Serializable
    data class ResponseMissingEntries(
        val entries: List<Entry>,
        override val requestId: String? = null
    ) : GameEvent()

    @Serializable
    data class Success(
        val message: String,
        override val requestId: String? = null
    ) : GameEvent()

    @Serializable
    data class Error(
        val message: String,
        override val requestId: String? = null
    ) : GameEvent()

    @Serializable
    data class GameComplete(val game: Game, override val requestId: String? = null
    ) : GameEvent()
}
