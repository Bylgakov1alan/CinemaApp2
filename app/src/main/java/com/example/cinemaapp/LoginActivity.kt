package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)

        Log.d("LoginActivity", "onCreate called with intent: $intent")
        intent.data?.let { uri ->
            Log.d("LoginActivity", "Received redirect: $uri")
            val email = uri.getQueryParameter("email")
            if (email != null) {
                Log.d("LoginActivity", "Email from redirect: $email")
                emailEditText.setText(email)
                Toast.makeText(this, "Verification successful. Please log in.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("LoginActivity", "No email in redirect")
            }
        } ?: Log.d("LoginActivity", "No intent data received")

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.0.108:5000/api/login")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val token = jsonResponse.getString("token")
                    saveToken(token)
                    runOnUiThread {
                        // Запускаем MainActivity и передаём флаг для открытия ProfileFragment
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("navigate_to_profile", true)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    @SuppressLint("UseKtx")
    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            putLong("token_timestamp", System.currentTimeMillis()) // Track when token was saved
            apply()
        }
        Log.d("LoginActivity", "Token saved: $token")
    }

    private fun isTokenExpired(): Boolean {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val timestamp = sharedPreferences.getLong("token_timestamp", 0)
        val expirationTime = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        return System.currentTimeMillis() - timestamp > expirationTime
    }}