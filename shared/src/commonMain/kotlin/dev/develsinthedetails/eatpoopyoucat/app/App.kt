package dev.develsinthedetails.eatpoopyoucat.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val appSettings: AppSettings = koinInject()
    val isReady by appSettings.isReadyFlow.collectAsState()
    AppTheme {
        if (!isReady) {
            Spinner()
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                NavGraph(
                    netGameParams = null,
                    onNetGameParamsConsumed = { },
                )
            }
        }
    }
}
