package com.example.cinemaapp

import com.example.cinemaapp.models.FavoriteRequest
import com.example.cinemaapp.models.FavoriteResponse
import com.example.cinemaapp.models.Movie
import com.example.cinemaapp.models.RegisterRequest
import com.example.cinemaapp.models.RegisterResponse
import com.example.cinemaapp.models.VerifyCodeRequest
import com.example.cinemaapp.models.VerifyCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MovieApiService {
    @POST("register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("verify-code")
    fun verifyCode(@Body request: VerifyCodeRequest): Call<VerifyCodeResponse>

    @GET("api/movies")
    fun getMovies(): Call<List<Movie>>

    @POST("api/favorites/add")
    fun addToFavorites(@Body request: FavoriteRequest): Call<FavoriteResponse>

    @POST("api/favorites/remove")
    fun removeFromFavorites(@Body request: FavoriteRequest): Call<FavoriteResponse>

    @GET("api/favorites")
    fun getFavorites(): Call<List<Movie>>
}