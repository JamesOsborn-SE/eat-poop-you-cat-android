package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class BackupFileSaver(
    private val launchBlock: (ByteArray, String, String) -> Unit
) {
    actual fun saveBackup(bytes: ByteArray, suggestedName: String, extension: String) {
        launchBlock(bytes, suggestedName, extension)
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberBackupFileSaver(
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
): BackupFileSaver {
    return remember {
        BackupFileSaver { bytes, name, ext ->
            try {
                val base64 = Base64.encode(bytes)
                val dataUrl = "data:application/gzip;base64,$base64"

                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = dataUrl
                anchor.download = "$name.$ext"

                document.body?.appendChild(anchor)
                anchor.click()
                document.body?.removeChild(anchor)

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}