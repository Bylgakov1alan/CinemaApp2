package com.example.cinemaapp

import Movie

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HorizontalSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = spacing
    }
}

class HomeFragment : Fragment() {
    private lateinit var movieApiService: MovieApiService
    private lateinit var recommendedAdapter: MovieAdapter
    private lateinit var continueAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movieApiService = MovieApiService.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)

        recommendedAdapter = MovieAdapter(emptyList()) { movie ->
            openMovieDetails(movie)
        }
        continueAdapter = MovieAdapter(emptyList()) { movie ->
            openMovieDetails(movie)
        }

        view.findViewById<RecyclerView>(R.id.recommendedRecyclerView).apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recommendedAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        view.findViewById<RecyclerView>(R.id.continueRecyclerView).apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = continueAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        lifecycleScope.launch {
            try {
                val movies = movieApiService.getMovies()
                Log.d("HomeFragment", "Movies loaded: ${movies.size}")
                recommendedAdapter.updateMovies(movies)
                continueAdapter.updateMovies(movies.shuffled())
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading movies", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return view
    }

    private fun openMovieDetails(movie: Movie) {
        val bundle = Bundle().apply {
            putParcelable("movie", movie)
        }
        val detailsFragment = MovieDetailsFragment().apply {
            arguments = bundle
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.content_frame, detailsFragment)
            .addToBackStack("details")
            .commit()
    }
}