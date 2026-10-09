package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.NavigationCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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