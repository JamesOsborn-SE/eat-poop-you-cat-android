package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.w3c.dom.HTMLTextAreaElement

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun shareLink(link: String) {
    val textArea = document.createElement("textarea") as HTMLTextAreaElement
    textArea.value = link

    textArea.style.position = "fixed"
    textArea.style.left = "-9999px"

    document.body?.appendChild(textArea)
    textArea.select()

    try {
        val successful = document.execCommand("copy")
        if (!successful) println("Browser blocked the copy action")
    } catch (e: Exception) {
        println("Copy failed: ${e.message}")
    } finally {
        document.body?.removeChild(textArea)
    }
}