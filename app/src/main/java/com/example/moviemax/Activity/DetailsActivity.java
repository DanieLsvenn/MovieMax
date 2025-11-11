package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";

    private ImageView ivMoviePoster, ivMovieBackdrop;
    private TextView tvMovieTitle, tvMovieGenre, tvMovieDuration, tvMovieLanguage;
    private TextView tvMovieDirector, tvMovieCast, tvMovieReleaseDate, tvMovieDescription, tvMovieRating;
    private TextView tvMovieGenreAndDuration;
    private ImageButton btnBack;
    private Button btnBookTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();
        setupClickListeners();
        loadMovieDetails();
    }

    private void initViews() {
        // Hero section
        ivMoviePoster = findViewById(R.id.ivMoviePoster);
        ivMovieBackdrop = findViewById(R.id.ivMovieBackdrop);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMovieRating = findViewById(R.id.tvMovieRating);
        tvMovieGenreAndDuration = findViewById(R.id.tvMovieGenreAndDuration);

        // Details section
        tvMovieGenre = findViewById(R.id.tvMovieGenre);
        tvMovieDuration = findViewById(R.id.tvMovieDuration);
        tvMovieLanguage = findViewById(R.id.tvMovieLanguage);
        tvMovieDirector = findViewById(R.id.tvMovieDirector);
        tvMovieCast = findViewById(R.id.tvMovieCast);
        tvMovieReleaseDate = findViewById(R.id.tvMovieReleaseDate);
        tvMovieDescription = findViewById(R.id.tvMovieDescription);

        // Buttons
        btnBack = findViewById(R.id.btnBack);
        btnBookTickets = findViewById(R.id.btnBookTickets);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnBookTickets.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(DetailsActivity.this, ShowTimeActivity.class);
            int movieId = getIntent().getIntExtra("movie_id", -1);
            if (movieId != -1) {
                bookingIntent.putExtra("MOVIE_ID", movieId);
            }
            startActivity(bookingIntent);
        });
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

        // Set hero section data
        tvMovieTitle.setText(title != null ? title : "Unknown Title");
        tvMovieRating.setText(String.valueOf(rating));

        String genreAndDuration = (genre != null ? genre : "Unknown Genre") + " • " + duration + " min";
        tvMovieGenreAndDuration.setText(genreAndDuration);

        // Set details section data
        tvMovieGenre.setText(genre != null ? genre : "Unknown Genre");
        tvMovieDuration.setText(duration + " minutes");
        tvMovieLanguage.setText(language != null ? language : "Unknown Language");
        tvMovieDirector.setText(director != null ? director : "Unknown Director");
        tvMovieCast.setText(cast != null ? cast : "Unknown Cast");
        tvMovieReleaseDate.setText(releaseDate != null ? releaseDate : "Unknown Date");
        tvMovieDescription.setText(description != null ? description : "No description available");

        // Load poster images
        loadPosterImage(posterUrl);
    }

    private void loadPosterImage(String posterUrl) {
        if (posterUrl == null || posterUrl.isEmpty()) {
            Log.w(TAG, "Poster URL is empty");
            return;
        }

        // Load main poster
        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivMoviePoster);

        // Load backdrop (same image with reduced opacity handled by layout)
        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivMovieBackdrop);
    }

    /**
     * Convert poster URL to full Supabase URL if needed
     * @param posterUrl Either a filename or full URL
     * @return Full public URL
     */
}