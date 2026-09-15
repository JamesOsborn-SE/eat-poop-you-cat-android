package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.develsinthedetails.eatpoopyoucat.app.App
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.ic_launcher_foreground
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        FileKit.init(appId = "EatPoopYouCat")

        Window(
            onCloseRequest = ::exitApplication,
            title = "Eat Poop You Cat",
            icon = painterResource(Res.drawable.ic_launcher_foreground),
        ) {
            App()
        }
    }
}