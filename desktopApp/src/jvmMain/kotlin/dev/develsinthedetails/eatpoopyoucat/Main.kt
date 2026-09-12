package dev.develsinthedetails.eatpoopyoucat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.develsinthedetails.eatpoopyoucat.app.NavGraph
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.startKoin

fun main() {
    // 1. Initialize global dependencies ONCE before the UI loop starts
    startKoin {
        modules(appModule)
    }

    application {
        FileKit.init(appId = "EatPoopYouCat")

        Window(
            onCloseRequest = ::exitApplication,
            title = "Eat Poop You Cat",
//            icon = getDrawable(Res.drawable.ic_launcher_foreground),
        ) {
            AppTheme {
                NavGraph(
                    netGameParams = null,
                    onNetGameParamsConsumed = { },
                )
            }
        }
    }
}