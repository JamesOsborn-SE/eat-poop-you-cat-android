package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.develsinthedetails.eatpoopyoucat.app.App
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModule)
    }
    val targetElement = document.getElementById("compose-target") ?: document.body!!
    ComposeViewport(targetElement) {
        App()
    }
}

