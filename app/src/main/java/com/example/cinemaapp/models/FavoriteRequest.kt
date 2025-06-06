package com.example.cinemaapp.models

import com.google.gson.annotations.SerializedName

data class FavoriteRequest(
    @SerializedName("movie_id")
    val movieId: Int
)