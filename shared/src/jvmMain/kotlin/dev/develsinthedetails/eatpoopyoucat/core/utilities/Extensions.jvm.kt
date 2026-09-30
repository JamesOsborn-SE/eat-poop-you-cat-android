package dev.develsinthedetails.eatpoopyoucat.core.utilities

import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@androidx.compose.runtime.Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun shareLink(link: String) {
    if (GraphicsEnvironment.isHeadless()) {
        println("Headless environment detected. Cannot copy to clipboard.")
        return
    }
    val selection = StringSelection(link)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(selection, null)
}