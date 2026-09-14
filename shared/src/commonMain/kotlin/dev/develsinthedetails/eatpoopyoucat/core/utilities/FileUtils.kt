package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import kotlin.time.Clock

fun defaultImageFilename(): String {
    return "EPYC-${Clock.System.now().saveDateFormat()}.png"
}

fun defaultDataFilename(): String {
    return "EPYC-${Clock.System.now().saveDateFormat()}.json"
}

internal interface ShareFileLauncher {
    val isSupported: Boolean

    fun launch(files: List<PlatformFile>)
    fun launch(file: PlatformFile) {
        launch(listOf(file))
    }
}

@Composable
internal expect fun rememberShareFileLauncher(
    onError: (FileKitDialogException) -> Unit,
): ShareFileLauncher
