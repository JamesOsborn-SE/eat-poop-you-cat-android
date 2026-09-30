package dev.develsinthedetails.eatpoopyoucat.app

import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import kotlin.uuid.Uuid

sealed interface NavigationCommand {
    data class OpenDraw(
        val gameId: Uuid,
        val gameMode: GameMode
    ) : NavigationCommand

    data class OpenSentence(
        val gameId: Uuid,
        val gameMode: GameMode
    ) : NavigationCommand

    data class OpenPreviousGameDetails(
        val gameId: Uuid
    ) : NavigationCommand
}
