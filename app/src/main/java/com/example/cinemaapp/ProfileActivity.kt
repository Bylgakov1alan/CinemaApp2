package com.example.cinemaapp


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация TextView и Button
        val profileText = findViewById<TextView>(R.id.profileText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Установка текста профиля (например, имя пользователя из SharedPreferences)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Guest")
        profileText.text = "Welcome, $userName!"

        // Обработчик нажатия кнопки выхода
        logoutButton.setOnClickListener {
            // Очистка данных пользователя
            with(sharedPreferences.edit()) {
                clear() // Удаляет все данные, включая user_name
                apply()
            }

            // Переход на экран логина и закрытие текущего активити
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}