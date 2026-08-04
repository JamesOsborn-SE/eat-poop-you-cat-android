package dev.develsinthedetails.eatpoopyoucat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ROUTE_TO


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SharedPref.init(applicationContext)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val route = intent.getStringExtra(ROUTE_TO)
        setContent {
            AppTheme {
                EatPoopYouCatApp(goto = route)
            }
        }
    }
}

