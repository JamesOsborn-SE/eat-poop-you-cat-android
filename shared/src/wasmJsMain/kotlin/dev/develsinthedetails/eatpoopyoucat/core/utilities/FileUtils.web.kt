@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun checkShareSupported(): Boolean = js("!!navigator.share")

private fun shareFileJs(base64Data: String, fileName: String, mimeType: String) {
    js(
        """
        if (!navigator.share) return;
        
        // Decode Base64 to a raw byte array in JS
        const byteString = atob(base64Data);
        const ab = new ArrayBuffer(byteString.length);
        const ia = new Uint8Array(ab);
        for (let i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }
        
        // Construct the file and share it
        const file = new window.File([ab], fileName, { type: mimeType });
        if (navigator.canShare && navigator.canShare({ files: [file] })) {
            navigator.share({
                files: [file],
                title: fileName
            }).catch(err => console.error("Share failed:", err));
        } else {
            console.warn("File sharing is not supported by this specific browser.");
        }
    """
    )
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberShareFileLauncher(
    onError: (Exception) -> Unit
): ShareFileLauncher {
    val supported = remember { checkShareSupported() }

    return remember(supported) {
        object : ShareFileLauncher {
            override val isSupported: Boolean = supported

            override fun launch(bytes: ByteArray, fileName: String) {
                try {
                    val base64 = Base64.encode(bytes)

                    shareFileJs(base64, fileName, "image/png")
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }
}
