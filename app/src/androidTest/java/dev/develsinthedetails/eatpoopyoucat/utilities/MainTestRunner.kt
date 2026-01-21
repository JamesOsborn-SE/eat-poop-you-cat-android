package dev.develsinthedetails.eatpoopyoucat.utilities

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
class KoinTestApplication : Application()

class MainTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, KoinTestApplication::class.java.name, context)
    }
}