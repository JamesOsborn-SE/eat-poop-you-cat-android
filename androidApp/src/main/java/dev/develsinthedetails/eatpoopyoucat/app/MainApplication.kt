package dev.develsinthedetails.eatpoopyoucat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.StrictMode
import dev.develsinthedetails.eatpoopyoucat.BuildConfig
import dev.develsinthedetails.eatpoopyoucat.di.androidModule
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import dev.develsinthedetails.eatpoopyoucat.di.databaseModule
import dev.develsinthedetails.eatpoopyoucat.feature.notifications.AppNotificationChannel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build()
            )
        }
        createNotificationChannels()
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(databaseModule, appModule, androidModule)
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        manager?.createNotificationChannels(
            AppNotificationChannel.entries.map {
                NotificationChannel(it.id, it.channelName, it.importance)
            }
        )
    }
}