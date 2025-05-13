package com.example.cinemaapp

import Movie
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.net.URLEncoder

class MovieAdapter(
    private var movies: List<Movie>,
    private val onClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.moviePoster)
        val title: TextView = itemView.findViewById(R.id.movieTitle)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        val posterUrl = "${MovieApiService.BASE_URL}poster/${URLEncoder.encode(movie.posterUrl.split("/").last(), "UTF-8")}"

        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .placeholder(R.drawable.ic_android_black_24dp)
            .error(R.drawable.error_poster)
            .into(holder.poster)

        holder.title.text = movie.title
        holder.itemView.setOnClickListener { onClick(movie) }
    }

    override fun getItemCount() = movies.size

}