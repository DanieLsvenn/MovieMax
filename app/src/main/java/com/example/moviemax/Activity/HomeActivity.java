package com.example.moviemax.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.MovieAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ProgressBar progressBar;
    private TextView tvError;
    private List<MovieResponse> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        initViews();
        setupRecyclerView();
        loadMovies();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMovies);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        movieList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        movieAdapter = new MovieAdapter(this, movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        Call<List<MovieResponse>> call = ApiService.getMovieApiService().getMovies();
        call.enqueue(new Callback<List<MovieResponse>>() {
            @Override
            public void onResponse(Call<List<MovieResponse>> call, Response<List<MovieResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("API_RESPONSE", "Movies API Response Code: " + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("API_RESPONSE_ERROR", "Error body: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Movies loaded successfully: " + response.body().size() + " movies");
                    movieList.clear();
                    movieList.addAll(response.body());
                    movieAdapter.updateMovies(movieList);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    showError("Failed to load movies. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MovieResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Movies API Error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
