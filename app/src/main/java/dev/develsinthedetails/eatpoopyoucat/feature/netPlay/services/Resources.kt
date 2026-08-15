package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Resource("/ping/{gameId}")
class Ping(val gameId: Uuid)

/**
 * Gets game and roster of player for new to you game
 * returns: [dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters] or 404
 */
@Serializable
@Resource("/game/{gameId}")
class GetGameWithRosters(val gameId: Uuid)

/**
 * Post
 * body: [dev.develsinthedetails.eatpoopyoucat.data.models.Roster]
 * response: ok or conflict
 */
@Serializable
@Resource("/join")
class JoinGame

/**
 * no body
 */
@Serializable
@Resource("/game/{gameId}/turn")
class AskTakeTurn(val gameId: Uuid)

/**
 * body (required): [dev.develsinthedetails.eatpoopyoucat.data.models.Entry]
 */
@Serializable
@Resource("/game/turn/")
class TakeTurn

/**
 * Check for roster updates
 * body (required): [dev.develsinthedetails.eatpoopyoucat.data.models.RosterHashAndCount]
 * response: Ok or GameWithRosters
 */
@Serializable
@Resource("/game/update/{gameId}/roster")
class UpdateRoster(val gameId: Uuid)

/**
 *
 * body (required): sequences already known [List<Int>]
 *
 * returns: Missing entries List<[dev.develsinthedetails.eatpoopyoucat.data.models.Entry]> may be empty
 */
@Serializable
@Resource("/game/update/{gameId}")
class UpdateGame(val gameId: Uuid)