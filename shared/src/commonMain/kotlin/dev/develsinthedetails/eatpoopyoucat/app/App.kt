package dev.develsinthedetails.eatpoopyoucat.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameClient
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val appSettings: AppSettings = koinInject()
    val notifier: Notifier = koinInject()
    val client: GameClient = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(notifier, snackbarHostState) {
        val jobs = mutableMapOf<String, Job>()

        notifier.events.collect { command ->
            when (command) {
                is SnackbarCommand.Show -> {
                    val event = command.event
                    val job = launch(start = CoroutineStart.LAZY) {
                        try {
                            val result = snackbarHostState.showSnackbar(
                                message = event.message,
                                actionLabel = event.actionLabel,
                                duration = if (event.actionLabel != null) {
                                    SnackbarDuration.Indefinite
                                } else {
                                    SnackbarDuration.Short
                                },
                            )

                            if (result == SnackbarResult.ActionPerformed) {
                                event.onAction?.invoke()
                            }
                            if (result == SnackbarResult.Dismissed && event.id != Uuid.NIL) {
                                client.dismissTurn(event.id)
                            }
                        } finally {
                            jobs.remove(event.id.toString())
                        }
                    }

                    jobs[event.id.toString()] = job
                    job.start()
                }

                is SnackbarCommand.Dismiss -> {
                    jobs.remove(command.id)?.cancel()
                }
            }
        }
    }

    val isReady by appSettings.isReadyFlow.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppTheme {
            CompositionLocalProvider(LocalAppNotifier provides notifier) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) {
                    if (!isReady) {
                        Spinner()
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavGraph(
                                onNavHostReady = onNavHostReady
                            )
                        }
                    }
                }
            }
        }
    }
}
