package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemaapp.models.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n", "UseKtx")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            redirectToLogin()
            return view
        }

        // Инициализация TextView, Button и RecyclerView
        val profileText = view.findViewById<TextView>(R.id.profileText)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val favoritesRecyclerView = view.findViewById<RecyclerView>(R.id.favoritesRecyclerView)

        // Настройка RecyclerView
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            showMovieDetails(movie)
        }
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = movieAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        // Установка текста профиля
        val userName = sharedPreferences.getString("user_name", "Guest")
        profileText.text = "Welcome, $userName!"

        // Загрузка избранного
        loadFavorites()

        // Обработчик нажатия кнопки выхода
        logoutButton.setOnClickListener {
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            redirectToLogin()
        }

        return view
    }

    private fun loadFavorites() {
        ApiClient.apiService.getFavorites().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    movieAdapter.updateMovies(favorites)
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки избранного: ${response.code()}", Toast.LENGTH_SHORT).show()
                    if (response.code() == 401) {
                        redirectToLogin()
                    }
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showMovieDetails(movie: Movie) {
        val detailsFragment = MovieDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("movie", movie)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.content_frame, detailsFragment)
            .addToBackStack("details") // Добавляем в стек, чтобы можно было вернуться назад
            .commit()
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}