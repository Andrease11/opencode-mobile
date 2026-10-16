package io.github.beppe2880.opencodemobile

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpenCodeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenCodeApplication)
            modules(dataModules() + appModule)
        }
    }
}
