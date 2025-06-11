package com.example.cinemaapp

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
    private lateinit var recommendationsAdapter: MovieAdapter

    @SuppressLint("SetTextI18n", "UseKtx")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return view
        }

        val profileText = view.findViewById<TextView>(R.id.profileText)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val favoritesRecyclerView = view.findViewById<RecyclerView>(R.id.favoritesRecyclerView)

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

        val recommendationsRecyclerView = view.findViewById<RecyclerView>(R.id.recommendationsRecyclerView)
        recommendationsAdapter = MovieAdapter(emptyList()) { movie ->
            showMovieDetails(movie)
        }
        recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recommendationsAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        val userName = sharedPreferences.getString("user_name", "Guest")
        profileText.text = "Welcome, $userName!"

        loadFavorites()
        loadRecommendations()

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
        if (!isAdded) return // Check if fragment is attached
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        ApiClient.apiService.getFavorites().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (!isAdded) return // Ensure fragment is still attached
                if (response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    movieAdapter.updateMovies(favorites)
                } else {
                    Toast.makeText(context, "Ошибка загрузки избранного: ${response.code()}", Toast.LENGTH_SHORT).show()
                    if (response.code() == 401) {
                        redirectToLogin()
                    }
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(context, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadRecommendations() {
        if (!isAdded) return
        val token = sharedPreferences.getString("auth_token", null)
        Log.d("ProfileFragment", "Loading recommendations, Token: $token")
        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }
        Log.d("ProfileFragment", "Token: $token")
        ApiClient.apiService.getRecommendations().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (!isAdded) return
                when (response.code()) {
                    in 200..299 -> {
                        val recommendations = response.body() ?: emptyList()
                        recommendationsAdapter.updateMovies(recommendations)
                    }
                    401 -> {
                        Toast.makeText(context, "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show()
                        redirectToLogin()
                    }
                    else -> Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                if (isAdded) Toast.makeText(context, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showMovieDetails(movie: Movie) {
        if (!isAdded) return
        val detailsFragment = MovieDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("movie", movie)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.content_frame, detailsFragment)
            .addToBackStack("details")
            .commit()
    }

    private fun redirectToLogin() {
        if (!isAdded) return
        val intent = Intent(requireContext(), RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}