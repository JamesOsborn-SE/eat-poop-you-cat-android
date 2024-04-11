package dev.develsinthedetails.eatpoopyoucat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.develsinthedetails.eatpoopyoucat.ui.EatPoopYouCatApp
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        SharedPref.init(applicationContext)
        super.onCreate(savedInstanceState)
        val goto = intent.extras?.getString("routeTo")
        setContent {
            AppTheme {
                EatPoopYouCatApp(goto = goto)
            }
        }

    }
}

