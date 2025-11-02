package com.example.moviemax.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.R;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<MovieResponse> movies;
    private Context context;

    public MovieAdapter(Context context, List<MovieResponse> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieResponse movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());
        holder.tvDuration.setText(movie.getDuration() + " min");
        holder.tvRating.setText(String.valueOf(movie.getRating()));

        // Load poster image with Glide
        String posterUrl = "http://103.200.20.174:8081/images/" + movie.getPosterUrl();
        Glide.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivPoster);

        // Set click listener to open movie details
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, DetailsActivity.class);
//            intent.putExtra("movie_title", movie.getTitle());
//            intent.putExtra("movie_genre", movie.getGenre());
//            intent.putExtra("movie_duration", movie.getDuration());
//            intent.putExtra("movie_language", movie.getLanguage());
//            intent.putExtra("movie_director", movie.getDirector());
//            intent.putExtra("movie_cast", movie.getCast());
//            intent.putExtra("movie_description", movie.getDescription());
//            intent.putExtra("movie_poster_url", movie.getPosterUrl());
//            intent.putExtra("movie_release_date", movie.getReleaseDate());
//            intent.putExtra("movie_rating", movie.getRating());
//            context.startActivity(intent);
//        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateMovies(List<MovieResponse> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvGenre, tvDuration, tvRating;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvGenre = itemView.findViewById(R.id.tvMovieGenre);
            tvDuration = itemView.findViewById(R.id.tvMovieDuration);
            tvRating = itemView.findViewById(R.id.tvMovieRating);
        }
    }
}
