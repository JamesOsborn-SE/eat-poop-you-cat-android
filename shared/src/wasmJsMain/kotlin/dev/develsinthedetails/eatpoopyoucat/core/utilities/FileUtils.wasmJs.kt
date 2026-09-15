package dev.develsinthedetails.eatpoopyoucat.core.utilities

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun saveToGallery(bytes: ByteArray, fileName: String) {
    val base64 = Base64.encode(bytes)
    val dataUrl = "data:image/png;base64,$base64"

    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = dataUrl
    anchor.download = fileName

    document.body?.appendChild(anchor)
    anchor.click()
    document.body?.removeChild(anchor)
}