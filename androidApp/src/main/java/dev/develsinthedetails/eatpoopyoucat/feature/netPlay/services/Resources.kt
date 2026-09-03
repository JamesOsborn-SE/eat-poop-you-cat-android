package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * GET
 *
 * check if alive
 *
 * Returns Status Ok
 */
@Serializable
@Resource("/ping")
class Ping

@Serializable
@Resource("/game")
class GameRoot {
    /**
     * GET /game/{id}
     *
     * Gets game and roster of player for new to you game
     *
     * returns: [dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters] or 404
     */
    @Serializable
    @Resource("{id}")
    class Id(val parent: GameRoot = GameRoot(), val id: Uuid) {
        /**
         * GET /game/{id}/turn
         *
         * body none
         */
        @Serializable
        @Resource("turn")
        class AskTakeTurn(val parent: Id)

        /**
         * GET /game/{id}/hash/{hash}/count/{count}
         *
         * Check for roster updates
         *
         * body: none
         *
         * response: Ok or GameWithRosters
         */
        @Serializable
        @Resource("hash/{hash}/count/{count}")
        class UpdateRoster(val parent: Id, val hash: String)


        /**
         * POST /game/{id}/update
         *
         * body (required): sequences already known [List<Int>]
         *
         * returns: Missing entries List<[dev.develsinthedetails.eatpoopyoucat.data.models.Entry]> may be empty
         */
        @Serializable
        @Resource("update")
        class UpdateGame(val parent: Id)
    }

    /**
     * POST /game/join
     *
     * body: [dev.develsinthedetails.eatpoopyoucat.data.models.Roster]
     *
     * response: ok or conflict
     */
    @Serializable
    @Resource("join")
    class JoinGame(val parent: GameRoot = GameRoot())

    /**
     * PUT /game/turn/
     *
     * body (required): [dev.develsinthedetails.eatpoopyoucat.data.models.Entry]
     */
    @Serializable
    @Resource("turn")
    class TakeTurn(val parent: GameRoot = GameRoot())


}