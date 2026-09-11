package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    return remember {
        object : NotificationPermissionState {
            override val hasPermission: Boolean = true
            override fun requestPermission() {
                // No-op: Not required on this platform
            }
        }
    }
}