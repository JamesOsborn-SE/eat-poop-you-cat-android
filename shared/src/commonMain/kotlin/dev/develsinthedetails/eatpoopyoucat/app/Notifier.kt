package dev.develsinthedetails.eatpoopyoucat.app

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @param message message to show
 * @param actionLabel label for action button. when set, snackbar will be indefinite and action will be shown
 * @param onAction action to perform when action button is clicked
 **/
data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (suspend () -> Unit)? = null,
)

class Notifier(private val scope: CoroutineScope) {
    private val _events = MutableSharedFlow<SnackbarEvent>(
        extraBufferCapacity = 16,
    )

    val events = _events.asSharedFlow()

    fun show(
        message: String,
        actionLabel: String? = null,
        onAction: (suspend () -> Unit)? = null,
    ) {
        scope.launch {
            _events.emit(SnackbarEvent(message, actionLabel, onAction))
        }
    }
}

val LocalAppNotifier = staticCompositionLocalOf<Notifier> {
    error("AppNotifier was not provided")
}