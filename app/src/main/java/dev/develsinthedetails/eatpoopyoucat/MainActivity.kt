package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.ROUTE_TO
import kotlinx.serialization.json.Json


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SharedPref.init(applicationContext)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val routeJson = intent.getStringExtra(ROUTE_TO)
        val gotoScreen: Screen? = try {
            routeJson?.let { Json.decodeFromString<Screen>(it) }
        } catch (e: Exception) {
            null
        }
        setContent {
            AppTheme {
                EatPoopYouCatApp(goto = gotoScreen)
            }
        }
    }
}

