package com.example.moviemax.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviemax.Activity.DetailsActivity;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.R;
//import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<MovieResponse> movies;
    private Context context;

    public BannerAdapter(Context context, List<MovieResponse> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        MovieResponse movie = movies.get(position);
        
        // Set movie data
        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());
        holder.tvRating.setText(String.valueOf(movie.getRating()));
        
        // Show premium format badge for high-rated movies
        if (movie.getRating() >= 8.0) {
            holder.tvFormatBadge.setVisibility(View.VISIBLE);
            holder.tvFormatBadge.setText("PREMIUM");
        } else {
            holder.tvFormatBadge.setVisibility(View.GONE);
        }

        // Load poster and background images
        String posterUrl = movie.getPosterUrl();
//        String fullPosterUrl = getFullPosterUrl(posterUrl);
//
//        // Load poster
//        Glide.with(context)
//                .load(fullPosterUrl)
//                .placeholder(R.drawable.ic_launcher_background)
//                .error(R.drawable.ic_launcher_background)
//                .into(holder.ivPoster);
//
//        // Load background (same image)
//        Glide.with(context)
//                .load(fullPosterUrl)
//                .placeholder(R.drawable.ic_launcher_background)
//                .error(R.drawable.ic_launcher_background)
//                .into(holder.ivBackground);

        // Book Now button click
        holder.btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("movie_title", movie.getTitle());
            intent.putExtra("movie_genre", movie.getGenre());
            intent.putExtra("movie_duration", movie.getDuration());
            intent.putExtra("movie_language", movie.getLanguage());
            intent.putExtra("movie_director", movie.getDirector());
            intent.putExtra("movie_cast", movie.getCast());
            intent.putExtra("movie_description", movie.getDescription());
            intent.putExtra("movie_poster_url", movie.getPosterUrl());
            intent.putExtra("movie_release_date", movie.getReleaseDate());
            intent.putExtra("movie_rating", movie.getRating());
            context.startActivity(intent);
        });

        // Card click listener (same as book now)
        holder.itemView.setOnClickListener(v -> holder.btnBookNow.performClick());
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateMovies(List<MovieResponse> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

//    private String getFullPosterUrl(String posterUrl) {
//        if (posterUrl.startsWith("http")) {
//            return posterUrl;
//        } else if (posterUrl.startsWith("poster_")) {
//            return SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
//        } else {
//            return "http://103.200.20.174:8081/images/" + posterUrl;
//        }
//    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackground, ivPoster;
        TextView tvTitle, tvGenre, tvRating, tvFormatBadge;
        Button btnBookNow;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackground = itemView.findViewById(R.id.ivBannerBackground);
            ivPoster = itemView.findViewById(R.id.ivBannerPoster);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvGenre = itemView.findViewById(R.id.tvBannerGenre);
            tvRating = itemView.findViewById(R.id.tvBannerRating);
            tvFormatBadge = itemView.findViewById(R.id.tvFormatBadge);
            btnBookNow = itemView.findViewById(R.id.btnBannerBookNow);
        }
    }
}