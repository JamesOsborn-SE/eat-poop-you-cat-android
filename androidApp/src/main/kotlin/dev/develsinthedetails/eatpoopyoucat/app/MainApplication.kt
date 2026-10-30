package dev.develsinthedetails.eatpoopyoucat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.StrictMode
import dev.develsinthedetails.eatpoopyoucat.BuildConfig
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.AppNotificationChannel
import dev.develsinthedetails.eatpoopyoucat.feature.notifications.SystemNotifierListener
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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
            modules(appModule)
            modules(module { singleOf(::SystemNotifierListener) })
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