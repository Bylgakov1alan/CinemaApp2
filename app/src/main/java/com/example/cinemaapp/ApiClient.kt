package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("StaticFieldLeak")
object ApiClient {
    const val BASE_URL = "http://192.168.0.103:5000/"
    const val VIDEO_BASE = "http://192.168.0.103:5000/video/"

    private lateinit var context: Context // Для доступа к сохранённым данным

    // Инициализация (вызовется при старте приложения)
    fun initialize(context: Context) {
        this.context = context
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Помощник, который добавляет токен к запросам
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Достаём токен из памяти телефона
        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: ""

        // Если токен есть - добавляем его в заголовок
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        chain.proceed(newRequest)
    }

    // Настраиваем клиента с нашим помощником
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)   // для логов
        .addInterceptor(authInterceptor) // наш помощник с токеном
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val apiService: MovieApiService = retrofit.create(MovieApiService::class.java)
}