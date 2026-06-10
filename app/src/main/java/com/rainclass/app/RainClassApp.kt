package com.rainclass.app

import android.app.Application
import com.rainclass.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RainClassApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RainClassApp)
            modules(appModule)
        }
    }
}
