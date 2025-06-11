package com.example.cinemaapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.initialize(this)
    }
}