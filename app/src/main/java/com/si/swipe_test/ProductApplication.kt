package com.si.swipe_test

import android.app.Application
import com.si.swipe_test.di.appModule
import com.si.swipe_test.di.dataModule
import com.si.swipe_test.di.workManagerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ProductApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ProductApplication)
            modules(appModule, dataModule, workManagerModule)
        }
    }
}
