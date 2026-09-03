package dev.develsinthedetails.eatpoopyoucat.app

import android.app.Application
import android.os.StrictMode
import dev.develsinthedetails.eatpoopyoucat.BuildConfig
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import dev.develsinthedetails.eatpoopyoucat.di.databaseModule
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
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(databaseModule, appModule)
        }
    }
}