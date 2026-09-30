package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.http.Url
import kotlin.uuid.Uuid

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    return remember {
        object : NotificationPermissionState {
            override val hasPermission: Boolean = true
            override fun requestPermission() {
            }
        }
    }
}

actual fun getServerUrl(): Url = Url("http://localhost:$SERVER_PORT")

actual fun getGameIdFromUrl(): Uuid? = null