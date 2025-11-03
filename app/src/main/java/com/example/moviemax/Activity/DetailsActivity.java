package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DetailsActivity extends AppCompatActivity {
    private ImageView ivMoviePoster;
    private TextView tvMovieTitle, tvMovieGenre, tvMovieDuration, tvMovieLanguage;
    private TextView tvMovieDirector, tvMovieCast, tvMovieReleaseDate, tvMovieDescription, tvMovieRating;
    private ImageButton btnBack;
    private FloatingActionButton fabUploadTest; // For FAB option
    // private Button btnTestUpload; // For Button option - uncomment if using Option B

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();
        setupClickListeners();
        loadMovieDetails();
    }

    private void initViews() {
        ivMoviePoster = findViewById(R.id.ivMoviePoster);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMovieGenre = findViewById(R.id.tvMovieGenre);
        tvMovieDuration = findViewById(R.id.tvMovieDuration);
        tvMovieLanguage = findViewById(R.id.tvMovieLanguage);
        tvMovieDirector = findViewById(R.id.tvMovieDirector);
        tvMovieCast = findViewById(R.id.tvMovieCast);
        tvMovieReleaseDate = findViewById(R.id.tvMovieReleaseDate);
        tvMovieDescription = findViewById(R.id.tvMovieDescription);
        tvMovieRating = findViewById(R.id.tvMovieRating);
        btnBack = findViewById(R.id.btnBack);

        // Initialize upload test button (FAB option)
        fabUploadTest = findViewById(R.id.fabUploadTest);

        // OR for Button option, uncomment this:
        // btnTestUpload = findViewById(R.id.btnTestUpload);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // FAB click listener
        fabUploadTest.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, ImageUploadTestActivity.class);
            startActivity(intent);
        });

        // OR for Button option, uncomment this:
        // btnTestUpload.setOnClickListener(v -> {
        //     Intent intent = new Intent(DetailsActivity.this, ImageUploadTestActivity.class);
        //     startActivity(intent);
        // });
    }

    private void loadMovieDetails() {
        Intent intent = getIntent();

        String title = intent.getStringExtra("movie_title");
        String genre = intent.getStringExtra("movie_genre");
        int duration = intent.getIntExtra("movie_duration", 0);
        String language = intent.getStringExtra("movie_language");
        String director = intent.getStringExtra("movie_director");
        String cast = intent.getStringExtra("movie_cast");
        String releaseDate = intent.getStringExtra("movie_release_date");
        String description = intent.getStringExtra("movie_description");
        String posterUrl = intent.getStringExtra("movie_poster_url");
        double rating = intent.getDoubleExtra("movie_rating", 0.0);

        // Set the data to views
        tvMovieTitle.setText(title != null ? title : "Unknown Title");
        tvMovieGenre.setText(genre != null ? genre : "Unknown Genre");
        tvMovieDuration.setText(duration + " minutes");
        tvMovieLanguage.setText(language != null ? language : "Unknown Language");
        tvMovieDirector.setText(director != null ? director : "Unknown Director");
        tvMovieCast.setText(cast != null ? cast : "Unknown Cast");
        tvMovieReleaseDate.setText(releaseDate != null ? releaseDate : "Unknown Date");
        tvMovieDescription.setText(description != null ? description : "No description available");
        tvMovieRating.setText(String.valueOf(rating));

        // Load poster image - UPDATED TO SUPPORT SUPABASE
        if (posterUrl != null && !posterUrl.isEmpty()) {
            String fullPosterUrl = getFullPosterUrl(posterUrl);
            Glide.with(this)
                    .load(fullPosterUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivMoviePoster);
        }
    }

    private String getFullPosterUrl(String posterUrl) {
        Log.d("getFullPosterUrl", posterUrl);
        if (posterUrl.startsWith("http")) {
            // Already a full URL (Supabase full URL)
            return posterUrl;
        } else {
            // Supabase storage filename pattern
            return SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
        }
    }
}