package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getGameIdFromUrl
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.nextType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class NavigationManager {
    private val _commands = MutableSharedFlow<NavigationCommand>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()
    suspend fun navigate(command: NavigationCommand) {
        _commands.emit(command)
    }
}

class IncomingEventProcessor(
    private val client: Client,
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

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun process(event: GameEvent) {
        when (event) {
            is GameEvent.TurnComplete -> {
                println("Received TurnComplete event: $event")
                ensureGameMatchesServer(event.gameId)
            }

            is GameEvent.TakeYourTurn -> {
                println("Received TakeYourTurn event: $event")
                ensureGameMatchesServer(event.gameId)
                val entry = event.entry

                notifier.show(
                    title = "Your turn",
                    message = "It's your turn to play"
                )

                if (entry.nextType == EntryType.Drawing) {
                    navigationManager.navigate(
                        NavigationCommand.OpenDraw(
                            event.gameId,
                            GameMode.LAN
                        )
                    )
                } else {
                    navigationManager.navigate(
                        NavigationCommand.OpenSentence(
                            event.gameId,
                            GameMode.LAN
                        )
                    )
                }
            }

            is GameEvent.GameComplete -> {
                println("Received GameUpdated event: $event")
                if (event.game.turns == null) {
                    println("Received GameUpdated event with null turns: $event")
                    return
                }

                ensureGameMatchesServer(event.game.id)

                stop()
                client.close()
                client.destroy()
                navigationManager.navigate(NavigationCommand.OpenPreviousGameDetails(event.game.id))
            }

            is GameEvent.JoinGame -> {
                println("Received JoinGame event: $event")
                repository.addPlayer(event.player)
            }

            is GameEvent.RegisterPlayerId -> {
                val gameId = getGameIdFromUrl() ?: return
                ensureGameMatchesServer(gameId)
            }

            else -> {
                // Ignore RPC responses here. They are handled by rpc().
            }
        }
    }

    private suspend fun ensureGameMatchesServer(gameId: Uuid) {
        var myGame = repository.getGameWithEntries(gameId)
        if (myGame == null) {
            val gameWithRoster = client.getGame(gameId) ?: return
            repository.upsertGameWithRosters(gameWithRoster)
            myGame = GameWithEntries(gameWithRoster.game, emptyList())
        }

        val entries = client.updateGame(myGame)

        if (entries.isEmpty()) return //means we good
        repository.upsertEntries(entries)
    }
}