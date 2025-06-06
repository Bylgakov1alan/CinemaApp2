package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemaapp.models.RegisterRequest
import com.example.cinemaapp.models.RegisterResponse
import com.example.cinemaapp.models.VerifyCodeRequest
import com.example.cinemaapp.models.VerifyCodeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        val request = RegisterRequest(email, password, name)
        val call = ApiClient.apiService.registerUser(request)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    registeredEmail = registerResponse?.email
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Check your email for the code", Toast.LENGTH_SHORT).show()
                        emailEditText.visibility = View.GONE
                        passwordEditText.visibility = View.GONE
                        nameEditText.visibility = View.GONE
                        registerButton.visibility = View.GONE
                        codeEditText.visibility = View.VISIBLE
                        verifyButton.visibility = View.VISIBLE
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun verifyCode(email: String, code: String) {
        val request = VerifyCodeRequest(email, code)
        val call = ApiClient.apiService.verifyCode(request)

        call.enqueue(object : Callback<VerifyCodeResponse> {
            @SuppressLint("UseKtx")
            override fun onResponse(call: Call<VerifyCodeResponse>, response: Response<VerifyCodeResponse>) {
                if (response.isSuccessful) {
                    val verifyResponse = response.body()
                    val name = verifyResponse?.name
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Verification successful", Toast.LENGTH_SHORT).show()
                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("user_name", name)
                            apply()
                        }
                        startActivity(Intent(this@RegisterActivity, ProfileFragment::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Invalid code", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<VerifyCodeResponse>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Verification failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}