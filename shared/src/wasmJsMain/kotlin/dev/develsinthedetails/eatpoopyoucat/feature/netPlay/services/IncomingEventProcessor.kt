package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getGameIdFromUrl
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.nextType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class IncomingEventProcessor(
    private val client: GameClient,
    private val repository: AppRepository,
    private val notifier: Notifier,
    private val navigationManager: NavigationManager,
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        job?.cancel()

        job = scope.launch {
            client.events.collect { event ->
                process(event)
            }
        }
    }

    private suspend fun process(event: GameEvent) {
        when (event) {
            is GameEvent.TurnComplete -> {
                println("Received TurnComplete event: $event")
                client.ensureGameMatchesServer(event.gameId)
            }

            is GameEvent.TakeYourTurn -> {
                println("Received TakeYourTurn event: $event")
                client.ensureGameMatchesServer(event.gameId)
                val entry = event.entry

                val navTo = suspend {
                    if (entry.nextType == EntryType.Drawing) {
                        navigationManager.navigate(
                            NavigationCommand.OpenDraw(event.gameId, GameMode.LAN)
                        )
                    } else {
                        navigationManager.navigate(
                            NavigationCommand.OpenSentence(event.gameId, GameMode.LAN)
                        )
                    }
                }

                notifier.show(
                    id = event.turnId,
                    actionLabel = "Take You Turn?",
                    onAction = navTo,
                    message = "It's your turn to play"
                )
            }

            is GameEvent.GameComplete -> {
                println("Received GameUpdated event: $event")
                if (event.game.turns == null) {
                    println("Received GameUpdated event with null turns: $event")
                    return
                }
                client.ensureGameMatchesServer(event.game.id)
                navigationManager.navigate(NavigationCommand.OpenPreviousGameDetails(event.game.id))
            }

            is GameEvent.JoinGame -> {
                println("Received JoinGame event: $event")
                repository.addPlayer(event.player)
            }

            is GameEvent.GamesPlayerIsIn -> {
                println("Received GamesPlayerIsIn event: $event")
                if (event.gameIds.isNullOrEmpty())
                    return
                event.gameIds.forEach { gameId ->
                    client.ensureGameMatchesServer(gameId)
                }
            }

            is GameEvent.TooSlow -> {
                if (getGameIdFromUrl() == event.gameId)
                    navigationManager.navigate(NavigationCommand.OpenInProcessGameDetails(event.gameId))
                notifier.dismiss(event.turnId)
                notifier.show("too slow", Uuid.random())
            }

            else -> {
                // Ignore RPC responses here. They are handled by rpc().
            }
        }
    }

}