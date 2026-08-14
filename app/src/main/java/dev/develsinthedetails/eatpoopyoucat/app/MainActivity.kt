package dev.develsinthedetails.eatpoopyoucat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val appSettings: AppSettings by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            !appSettings.isReady
        }
        setContent {
            AppTheme {
                NavGraph()
            }
        }
    }
}

