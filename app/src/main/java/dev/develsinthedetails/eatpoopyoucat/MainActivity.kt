package dev.develsinthedetails.eatpoopyoucat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.develsinthedetails.eatpoopyoucat.compose.EatPoopYouCatApp
import dev.develsinthedetails.eatpoopyoucat.permissionsUtiliy.PermissionsUtility

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        SharedPref.init(applicationContext)
        super.onCreate(savedInstanceState)

        setContent {
            EatPoopYouCatApp()
        }

    }

    override fun onStart() {
        super.onStart()
        PermissionsUtility.requestPermissions(this)
    }
}

