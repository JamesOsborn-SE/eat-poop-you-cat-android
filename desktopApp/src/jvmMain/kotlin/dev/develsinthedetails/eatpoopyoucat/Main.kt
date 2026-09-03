package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Eat Poop You Cat"
    ) {
        // Call your shared Compose UI entry component here
        // App() 
    }
}
