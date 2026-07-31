package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.ui.ImportGames
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.ImportGames
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

