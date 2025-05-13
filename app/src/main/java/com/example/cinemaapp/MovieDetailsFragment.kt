package com.example.cinemaapp
// 1. Импорты (добавляются автоматически при нажатии Alt+Enter на ошибках)
import Movie
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


@Suppress("DEPRECATION")
class MovieDetailsFragment : Fragment() {
    // 2. Binding-переменная
    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!




    // 3. Создание View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 4. Логика после создания View
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
            // Здесь можно добавить обработку ошибки, например:
            Toast.makeText(requireContext(), "Error loading movie details", Toast.LENGTH_SHORT).show()
            return
        }

        val plot = binding.moviePlot
        var sostoyanyeplot = false

        plot.setOnClickListener {
            sostoyanyeplot = !sostoyanyeplot
            if (sostoyanyeplot) {
                plot.maxLines = Integer.MAX_VALUE // Показываем все строки
                plot.ellipsize = null           // Убираем троеточие
            } else {
                plot.maxLines = 2                // Ограничиваем 2 строками
                plot.ellipsize = TextUtils.TruncateAt.END // Добавляем троеточие
            }  }


        binding.backButton.setOnClickListener {
            // Для Activity
            if (activity is AppCompatActivity) {
                (activity as AppCompatActivity).onBackPressed()
            }        }


        // 2. Заполняем данные
        binding.movieTitle.text = movie.title
        binding.movieDescriptionFull.text = movie.description
        binding.moviePlot.text = movie.plot
        binding.movieGenre.text = movie.genre

        Glide.with(this)
            .load("${MovieApiService.BASE_URL}${movie.posterUrl}")
            .error(R.drawable.error_poster)
            .into(binding.moviePoster)



        binding.playButton.setOnClickListener {
            playVideo(movie)
        }


    }

    private fun playVideo(movie: Movie) {
        val videoUrl = movie.videoUrl ?: run {
            showError("URL видео отсутствует")
            return
        }

        // Формируем полный URL, если это необходимо
        val fullVideoUrl = if (videoUrl.startsWith("http")) {
            videoUrl // Уже полный URL
        } else {
            MovieApiService.VIDEO_BASE + videoUrl // Добавляем базовый URL
        }

        // Проверяем валидность URL
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}