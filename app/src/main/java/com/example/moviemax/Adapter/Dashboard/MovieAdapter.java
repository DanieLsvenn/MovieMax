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

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<MovieResponse> movieList;
    private OnItemClickListener listener;
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
        holder.bind(movie, listener);

        holder.itemView.setBackgroundColor(position == selectedPosition
                ? ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)
                : ContextCompat.getColor(context, android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(prev);
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
            int prev = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGenre, tvReleaseDate, tvRating;
        ImageView imgPoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvReleaseDate = itemView.findViewById(R.id.tvReleaseDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }

        public void bind(MovieResponse movie, OnItemClickListener listener) {
            tvTitle.setText(movie.getTitle());
            tvGenre.setText(movie.getGenre());
            tvReleaseDate.setText("Release: " + movie.getReleaseDate());
            tvRating.setText("⭐ " + movie.getRating());

//            // Use Glide or fallback image
//            Glide.with(itemView.getContext())
//                    .load(movie.getPosterUrl())
//                    .placeholder(R.drawable.ic_movie_placeholder)
//                    .into(imgPoster);

            itemView.setOnClickListener(v -> listener.onItemClick(movie));
        }
    }
}
