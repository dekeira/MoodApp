package com.ustinova.kronomood

import android.app.Application
import com.ustinova.kronomood.data.App

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        App.context = this
    }
}