package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.develsinthedetails.eatpoopyoucat.ui.ImportGames
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme

@AndroidEntryPoint
class ImportGamesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val finish: () -> Unit = { this.finish() }
        setContent {
            AppTheme {
                ImportGames(fileUri = uri, finish = finish)
            }
        }
    }
}

