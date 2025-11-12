package com.example.moviemax.Adapter.Dashboard;

import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context context;
    private final List<MovieResponse> movieList;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(MovieResponse movie);
    }

    public MovieAdapter(Context context, List<MovieResponse> movieList, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieResponse movie = movieList.get(position);
        holder.bind(movie, context);

        // Highlight selected item
        holder.itemView.setBackgroundColor(position == selectedPosition
                ? ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)
                : ContextCompat.getColor(context, android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(movie);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public void setSelectedMovie(MovieResponse movie) {
        int index = movieList.indexOf(movie);
        if (index != -1) {
            int previousPosition = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvGenre;
        private final TextView tvDuration;
        private final TextView tvReleaseDate;
        private final TextView tvRating;
        private final ImageView imgPoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvReleaseDate = itemView.findViewById(R.id.tvReleaseDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }

        public void bind(MovieResponse movie, Context context) {
            tvTitle.setText(movie.getTitle());
            tvGenre.setText("Genre: " + movie.getGenre());
            tvDuration.setText("Duration: " + movie.getDuration() + " min");
            tvReleaseDate.setText("Release: " + movie.getReleaseDate());
            tvRating.setText("⭐ " + movie.getRating());

            // Load poster image using Supabase
            String posterUrl = movie.getPosterUrl();
            String fullPosterUrl = getFullPosterUrl(posterUrl);

            Glide.with(context)
                    .load(fullPosterUrl)
                    .placeholder(R.drawable.cinema)
                    .error(R.drawable.cinema)
                    .into(imgPoster);
        }

        private String getFullPosterUrl(String posterUrl) {
            if (posterUrl == null || posterUrl.isEmpty()) {
                return "";
            }
            return posterUrl.startsWith("http") ? posterUrl : SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
        }
    }
}