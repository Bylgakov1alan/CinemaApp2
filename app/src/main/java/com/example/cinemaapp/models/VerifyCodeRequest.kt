package com.example.cinemaapp.models

data class VerifyCodeRequest(
    val email: String,
    val code: String
)