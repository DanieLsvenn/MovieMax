package com.example.moviemax.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.MovieApi;
import com.example.moviemax.Model.MovieDto.MovieResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {

    private int movieId = 8; // tui tu lay movie cua tui

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu có truyền movieId từ Intent
        if (getIntent().hasExtra("movieId")) {
            movieId = getIntent().getIntExtra("movieId", 1);
        }

        // Gọi API lấy dữ liệu
        loadMovieById(movieId);
    }

    private void loadMovieById(int movieId) {
        MovieApi api = ApiService.getClient().create(MovieApi.class);

        api.getMovieById(movieId).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movie = response.body();


                    Log.d("MovieActivity", "ID: " + movie.getId());
                    Log.d("MovieActivity", "Title: " + movie.getTitle());
                    Log.d("MovieActivity", "Genre: " + movie.getGenre());
                    Log.d("MovieActivity", "Duration: " + movie.getDuration());
                    Log.d("MovieActivity", "Director: " + movie.getDirector());
                    Log.d("MovieActivity", "Release Date: " + movie.getReleaseDate());
                    Log.d("MovieActivity", "Rating: " + movie.getRating());
                } else {
                    Toast.makeText(MovieActivity.this, "Không thể tải phim", Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("MovieActivity", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("MovieActivity", "API Error: " + t.getMessage());
            }
        });
    }
}