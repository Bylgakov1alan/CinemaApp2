package com.example.cinemaapp


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("StaticFieldLeak")
object ApiClient {
    const val BASE_URL = "http://192.168.0.108:5000/"

    private var context: Context? = null // Nullable to avoid memory leaks

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC // Changed to BASIC for performance
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val ctx = context ?: return@Interceptor chain.proceed(originalRequest)
        val prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: ""
        Log.d("ApiClient", "Request URL: ${originalRequest.url}, Token: $token")
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        chain.proceed(newRequest)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        val ctx = context ?: throw IllegalStateException("ApiClient not initialized")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: MovieApiService by lazy {
        retrofit.create(MovieApiService::class.java)
    }
}