package com.example.cinemaapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализируем ApiClient при запуске приложения
        ApiClient.initialize(this)
    }
}