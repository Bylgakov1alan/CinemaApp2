package com.example.cinemaapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cinemaapp.databinding.FragmentMovieDetailsBinding
import com.example.cinemaapp.models.FavoriteRequest
import com.example.cinemaapp.models.FavoriteResponse
import com.example.cinemaapp.models.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class MovieDetailsFragment : Fragment() {
    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!
    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val movie = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable("movie", Movie::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable("movie")
            }
        } catch (e: Exception) {
            null
        } ?: run {
            Toast.makeText(requireContext(), "Error loading movie details", Toast.LENGTH_SHORT).show()
            return
        }

        // Заполняем данные
        binding.movieTitle.text = movie.title
        binding.movieDescriptionFull.text = movie.description
        binding.moviePlot.text = movie.plot
        binding.movieGenre.text = movie.genre

        Glide.with(this)
            .load("${ApiClient.BASE_URL}poster/${movie.posterUrl}")
            .error(R.drawable.error_poster)
            .into(binding.moviePoster)

        // Логика для раскрытия/сворачивания сюжета
        val plot = binding.moviePlot
        var sostoyanyeplot = false
        plot.setOnClickListener {
            sostoyanyeplot = !sostoyanyeplot
            if (sostoyanyeplot) {
                plot.maxLines = Integer.MAX_VALUE
                plot.ellipsize = null
            } else {
                plot.maxLines = 2
                plot.ellipsize = TextUtils.TruncateAt.END
            }
        }

        binding.backButton.setOnClickListener {
            if (activity is AppCompatActivity) {
                (activity as AppCompatActivity).onBackPressed()
            }
        }

        binding.playButton.setOnClickListener {
            playVideo(movie)
        }

        // Проверяем, в избранном ли фильм
        checkIfFavorite(movie)

        // Обработчики кнопок избранного
        binding.addToFavoritesButton.setOnClickListener {
            addToFavorites(movie)
        }

        binding.removeFromFavoritesButton.setOnClickListener {
            removeFromFavorites(movie)
        }
    }

    private fun checkIfFavorite(movie: Movie) {
        ApiClient.apiService.getFavorites().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    isFavorite = favorites.any { it.id == movie.id }
                    updateFavoriteButtonVisibility()
                } else {
                    showError("Не удалось проверить избранное: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                showError("Ошибка при проверке избранного: ${t.message}")
            }
        })
    }

    private fun addToFavorites(movie: Movie) {
        val request = FavoriteRequest(movieId = movie.id)
        ApiClient.apiService.addToFavorites(request).enqueue(object : Callback<FavoriteResponse> {
            override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                if (response.isSuccessful) {
                    val favoriteResponse = response.body()
                    if (favoriteResponse?.error == null) {
                        isFavorite = true
                        updateFavoriteButtonVisibility()
                        showMessage("Фильм добавлен в избранное")
                    } else {
                        showError(favoriteResponse.error ?: "Неизвестная ошибка")
                    }
                } else {
                    showError("Не удалось добавить в избранное: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                showError("Ошибка при добавлении: ${t.message}")
            }
        })
    }

    private fun removeFromFavorites(movie: Movie) {
        val request = FavoriteRequest(movieId = movie.id)
        ApiClient.apiService.removeFromFavorites(request).enqueue(object : Callback<FavoriteResponse> {
            override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                if (response.isSuccessful) {
                    val favoriteResponse = response.body()
                    if (favoriteResponse?.error == null) {
                        isFavorite = false
                        updateFavoriteButtonVisibility()
                        showMessage("Фильм удалён из избранного")
                    } else {
                        showError(favoriteResponse.error?: "Неизвестная ошибка")
                    }
                } else {
                    showError("Не удалось удалить из избранного: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                showError("Ошибка при удалении: ${t.message}")
            }
        })
    }

    private fun updateFavoriteButtonVisibility() {
        if (isFavorite) {
            binding.addToFavoritesButton.visibility = View.GONE
            binding.removeFromFavoritesButton.visibility = View.VISIBLE
        } else {
            binding.addToFavoritesButton.visibility = View.VISIBLE
            binding.removeFromFavoritesButton.visibility = View.GONE
        }
    }

    private fun playVideo(movie: Movie) {
        val videoUrl = movie.videoUrl ?: run {
            showError("URL видео отсутствует для ${movie.title}")
            return
        }

        val fullVideoUrl = "${ApiClient.BASE_URL}movie/${videoUrl}"
        if (!isValidUrl(fullVideoUrl)) {
            showError("Некорректный URL видео")
            return
        }

        try {
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra("VIDEO_URL", fullVideoUrl)
            }
            Log.d("VideoURL", "Playing video from: $fullVideoUrl")
            startActivity(intent)
        } catch (e: Exception) {
            showError("Не удалось запустить видеоплеер")
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            android.webkit.URLUtil.isValidUrl(url)
        } catch (e: Exception) {
            false
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}