package dev.develsinthedetails.eatpoopyoucat.feature.importgames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class ImportGamesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val finish: () -> Unit = { this.finish() }
        setContent {
            AppTheme {
                val uri = intent.data
                val vm: ImportGames = koinViewModel { parametersOf(uri) }

                ImportGames(viewModel = vm, fileUri = uri, finish = finish)
            }
        }
    }
}