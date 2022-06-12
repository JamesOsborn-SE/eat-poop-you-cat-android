package dev.develsinthedetails.eatpoopyoucat

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}
