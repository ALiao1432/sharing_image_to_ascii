package com.redso.sharing_image_to_ascii.util

import android.app.Application

class MyApp: Application() {
    companion object {
        lateinit var me: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        me = this
    }
}