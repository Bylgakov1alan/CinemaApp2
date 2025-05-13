package com.example.cinemaapp

import Movie
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


interface MovieApiService {
    @GET("api/movies")
    suspend fun getMovies(): List<Movie>

    companion object {
        // для моего телефона http://192.168.0.101:5000/
        // для телефона андроид студио http://10.0.2.2:5000/
        const val BASE_URL = "http://192.168.0.101:5000/"
        const val VIDEO_BASE = BASE_URL


        fun create(): MovieApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MovieApiService::class.java)
        }
    }
}