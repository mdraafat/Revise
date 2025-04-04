package com.raafat.revise

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize our singleton
        AppSingleton.initialize(applicationContext)
    }
}