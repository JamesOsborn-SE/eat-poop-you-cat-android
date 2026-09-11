package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init(appId = "Eat Poop You Cat")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Eat Poop You Cat"
    ) {
        // Call your shared Compose UI entry component here
        // App()
    }
}
