package com.example.newsapp

import android.app.Application
import com.example.newsapp.data.datasources.remote.ApiClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(startModule, appModule)
        }
    }
}