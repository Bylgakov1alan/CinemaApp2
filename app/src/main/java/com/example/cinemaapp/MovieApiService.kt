package com.example.cinemaapp

import Movie
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


interface MovieApiService {
    @GET("api/movies")
    suspend fun getMovies(): List<Movie>

    companion object {
        const val BASE_URL = "http://192.168.0.101:5000/"
        const val VIDEO_BASE = BASE_URL

        fun create(): MovieApiService {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(MovieApiService::class.java)
        }
    }
}