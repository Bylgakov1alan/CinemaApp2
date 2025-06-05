package com.example.cinemaapp

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemaapp.models.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HorizontalSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.right = spacing
    }
}

class HomeFragment : Fragment() {
    private lateinit var recommendedAdapter: MovieAdapter
    private lateinit var continueAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Место, куда вставляем обновлённую часть для загрузки фильмов
        ApiClient.apiService.getMovies().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    val movies = response.body() ?: emptyList()
                    Log.d("HomeFragment", "Movies loaded: ${movies.size}")
                    recommendedAdapter.updateMovies(movies)
                    continueAdapter.updateMovies(movies.shuffled())
                } else {
                    Toast.makeText(requireContext(), "Failed to load movies: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                Log.e("HomeFragment", "Error loading movies", t)
                Toast.makeText(requireContext(), "Ошибка загрузки: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })

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