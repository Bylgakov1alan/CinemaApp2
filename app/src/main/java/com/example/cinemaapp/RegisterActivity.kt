package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var codeEditText: EditText
    private lateinit var verifyButton: Button
    private var registeredEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        nameEditText = findViewById(R.id.editTextName)
        registerButton = findViewById(R.id.buttonRegister)
        loginButton = findViewById(R.id.loginButton)
        codeEditText = findViewById(R.id.editTextCode)
        verifyButton = findViewById(R.id.buttonVerify)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                registerUser(email, password, name)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString()
            if (code.length == 6) {
                verifyCode(registeredEmail ?: "", code)
            } else {
                Toast.makeText(this, "Please enter a 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        json.put("name", name)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.0.101:5000/register")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    registeredEmail = jsonResponse.getString("email")
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Check your email for the code", Toast.LENGTH_SHORT).show()
                        // Показываем поле для кода и кнопку
                        emailEditText.visibility = View.GONE
                        passwordEditText.visibility = View.GONE
                        nameEditText.visibility = View.GONE
                        registerButton.visibility = View.GONE
                        codeEditText.visibility = View.VISIBLE
                        verifyButton.visibility = View.VISIBLE
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun verifyCode(email: String, code: String) {
        val json = JSONObject()
        json.put("email", email)
        json.put("code", code)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.0.101:5000/verify-code")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            @SuppressLint("UseKtx")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val name = jsonResponse.getString("name")
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Verification successful", Toast.LENGTH_SHORT).show()
                        // Сохраняем имя пользователя
                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("user_name", name)
                            apply()
                        }
                        // Переходим в ProfileActivity
                        startActivity(Intent(this@RegisterActivity, ProfileActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Invalid code", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}