package dev.develsinthedetails.eatpoopyoucat.app

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.uuid.Uuid

/**
 * @param message message to show
 * @param actionLabel label for action button. when set, snackbar will be indefinite and action will be shown
 * @param onAction action to perform when action button is clicked
 **/
data class SnackbarEvent(
    val id: Uuid,
    val message: String,
    val actionLabel: String? = null,
    val onAction: (suspend () -> Unit)? = null,
    val withDismissAction: Boolean = true,
)

sealed interface SnackbarCommand {
    data class Show(val event: SnackbarEvent) : SnackbarCommand
    data class Dismiss(val id: String) : SnackbarCommand
}

class Notifier {
    private val commands = Channel<SnackbarCommand>(Channel.UNLIMITED)
    val events = commands.receiveAsFlow()

    fun show(
        message: String,
        id: Uuid? = null,
        actionLabel: String? = null,
        onAction: (suspend () -> Unit)? = null,
    ) {
        var id = id
        if (id == null)
            id = Uuid.NIL
        commands.trySend(
            SnackbarCommand.Show(SnackbarEvent(id, message, actionLabel, onAction, true))
        )
    }

    fun dismiss(id: Uuid) {
        commands.trySend(SnackbarCommand.Dismiss(id.toString()))
    }
}

val LocalAppNotifier = staticCompositionLocalOf<Notifier> {
    error("AppNotifier was not provided")
}