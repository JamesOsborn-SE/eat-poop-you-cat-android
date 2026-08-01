package dev.develsinthedetails.eatpoopyoucat.app

import android.app.Application
import dev.develsinthedetails.eatpoopyoucat.di.appModule
import dev.develsinthedetails.eatpoopyoucat.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(databaseModule, appModule)
            androidFileProperties()
        }
    }
}