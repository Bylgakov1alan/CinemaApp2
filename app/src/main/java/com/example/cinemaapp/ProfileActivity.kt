package com.example.cinemaapp


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ProfileActivity : AppCompatActivity() {


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("user_name", "User") ?: "User"
        val profileText = findViewById<TextView>(R.id.profileText)
        profileText.text = "ПРОФИЛЬ ($name)"
    }
}