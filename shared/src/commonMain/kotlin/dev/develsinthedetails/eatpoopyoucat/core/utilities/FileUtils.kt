package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import java.util.Date

fun defaultImageFilename(): String {
    return "EPYC-${Date().saveDateFormat()}.png"
}

fun defaultDataFilename(): String {
    return "EPYC-${Date().saveDateFormat()}.json"
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
