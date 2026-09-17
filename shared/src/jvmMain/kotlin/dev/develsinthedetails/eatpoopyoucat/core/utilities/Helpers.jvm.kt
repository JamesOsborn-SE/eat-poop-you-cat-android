package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.http.Url

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

actual fun getRealAddress(address: Url): Url = address