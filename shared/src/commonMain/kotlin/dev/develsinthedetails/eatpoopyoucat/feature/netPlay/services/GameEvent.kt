package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed class GameEvent {
    abstract val gameId: Uuid
    abstract val requestId: Uuid?

    @Serializable
    data class GamesPlayerIsIn(
        val playerId: Uuid,
        val gameIds: List<Uuid>? = null,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    /**
     * Player registers their playerId with the server
     */
    @Serializable
    data class RegisterPlayerId(
        val playerId: Uuid,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    /**
     * Get all the game metadata and player roster for a given game.
     */
    @Serializable
    data class RequestNetGame(
        override val gameId: Uuid,
        override val requestId: Uuid? = null
    ) : GameEvent()

    /**
     * Returns the game metadata and player roster for a given game.
     */
    @Serializable
    data class ResponseNetGame(
        val game: NetGame?,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    /**
     * player Joins game reply with ResponseNetGame
     */
    @Serializable
    data class JoinGame(
        val player: Roster,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    /**
     * Player takes turn
     * Server -> Client
     * Reply with Success if successful
     */
    @Serializable
    data class TakeYourTurn(
        val entry: Entry?,
        val turnId: Uuid,
        override val gameId: Uuid,
        override val requestId: Uuid? = null
    ) : GameEvent()

    /**
     * Turn is complete
     * update dbs
     * client / server
     */
    @Serializable
    data class TurnComplete(
        val entry: Entry?,
        override val gameId: Uuid,
        override val requestId: Uuid? = null
    ) : GameEvent()

    /**
     * Client asks for missing Entries at end of game if missing entries
     * Reply with ResponseMissingEntries
     */
    @Serializable
    data class RequestMissingEntries(
        override val gameId: Uuid,
        val knownTurns: List<Int>,
        override val requestId: Uuid? = null
    ) : GameEvent()

    @Serializable
    data class ResponseMissingEntries(
        val entries: List<Entry>,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    @Serializable
    data class Success(
        val message: String,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    @Serializable
    data class Error(
        val message: String,
        override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    @Serializable
    data class GameComplete(
        val game: Game, override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()

    @Serializable
    data class TooSlow(
        override val gameId: Uuid, val turnId: Uuid, override val requestId: Uuid? = null
    ) : GameEvent()

    @Serializable
    data class NotRightNow(
        val turnId: Uuid, override val requestId: Uuid? = null,
        override val gameId: Uuid
    ) : GameEvent()
}


fun GameEvent.withRequestId(
    requestId: Uuid,
): GameEvent {
    return when (this) {
        is GameEvent.RequestNetGame ->
            copy(requestId = requestId)

        is GameEvent.JoinGame ->
            copy(requestId = requestId)

        is GameEvent.TakeYourTurn ->
            copy(requestId = requestId)

        is GameEvent.RequestMissingEntries ->
            copy(requestId = requestId)

        is GameEvent.ResponseNetGame ->
            copy(requestId = requestId)

        is GameEvent.ResponseMissingEntries ->
            copy(requestId = requestId)

        is GameEvent.Success ->
            copy(requestId = requestId)

        is GameEvent.Error ->
            copy(requestId = requestId)

        is GameEvent.TooSlow ->
            copy(requestId = requestId)

        else -> this
    }
}

fun GameEvent.withoutRequestId(): GameEvent {
    return when (this) {
        is GameEvent.RequestNetGame ->
            copy(requestId = null)

        is GameEvent.JoinGame ->
            copy(requestId = null)

        is GameEvent.TakeYourTurn ->
            copy(requestId = null)

        is GameEvent.RequestMissingEntries ->
            copy(requestId = null)

        is GameEvent.ResponseNetGame ->
            copy(requestId = null)

        is GameEvent.ResponseMissingEntries ->
            copy(requestId = null)

        is GameEvent.Success ->
            copy(requestId = null)

        is GameEvent.Error ->
            copy(requestId = null)

        is GameEvent.TooSlow ->
            copy(requestId = null)

        else -> this
    }
}