package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import kotlin.time.Clock

fun defaultImageFilename(): String {
    return "EPYC-${Clock.System.now().saveDateFormat()}.png"
}

fun defaultDataFilename(): String {
    return "EPYC-${Clock.System.now().saveDateFormat()}.json"
}

interface ShareFileLauncher {
    val isSupported: Boolean
    fun launch(bytes: ByteArray, fileName: String)
}

@Composable
expect fun rememberShareFileLauncher(
    onError: (Exception) -> Unit
): ShareFileLauncher

expect suspend fun saveToGallery(bytes: ByteArray, fileName: String)