package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.example.moviemax.Supabase.SupabaseStorageHelper;

// import jp.wasabeef.glide.transformations.BlurTransformation;

public class DetailsActivity extends AppCompatActivity {
    private ImageView ivMoviePoster, ivMovieBackdrop;
    private TextView tvMovieTitle, tvMovieGenre, tvMovieDuration, tvMovieLanguage;
    private TextView tvMovieDirector, tvMovieCast, tvMovieReleaseDate, tvMovieDescription, tvMovieRating;
    private TextView tvMovieGenreAndDuration;
    private ImageButton btnBack;
    private FloatingActionButton fabUploadTest; // For FAB option
    // private Button btnTestUpload; // For Button option - uncomment if using Option B
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

        // Initialize upload test button (FAB option)
        fabUploadTest = findViewById(R.id.fabUploadTest);

        // OR for Button option, uncomment this:
        // btnTestUpload = findViewById(R.id.btnTestUpload);
        btnBookTickets = findViewById(R.id.btnBookTickets);
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

        btnBookTickets.setOnClickListener(v -> {
            // Navigate to ShowTimeActivity with movie ID
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

        int movieId = intent.getIntExtra("movie_id", -1);
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

        // Set the data to hero section views
        tvMovieTitle.setText(title != null ? title : "Unknown Title");
        tvMovieRating.setText(String.valueOf(rating));

        // Combine genre and duration for hero section
        String genreAndDuration = (genre != null ? genre : "Unknown Genre") + " • " + duration + " min";
        tvMovieGenreAndDuration.setText(genreAndDuration);

        // Set the data to details section views
        tvMovieGenre.setText(genre != null ? genre : "Unknown Genre");
        tvMovieDuration.setText(duration + " minutes");
        tvMovieLanguage.setText(language != null ? language : "Unknown Language");
        tvMovieDirector.setText(director != null ? director : "Unknown Director");
        tvMovieCast.setText(cast != null ? cast : "Unknown Cast");
        tvMovieReleaseDate.setText(releaseDate != null ? releaseDate : "Unknown Date");
        tvMovieDescription.setText(description != null ? description : "No description available");

        // Load poster image - UPDATED TO SUPPORT SUPABASE
       if (posterUrl != null && !posterUrl.isEmpty()) {
           String fullPosterUrl = getFullPosterUrl(posterUrl);

           // Load main poster
           Glide.with(this)
                   .load(fullPosterUrl)
                   .placeholder(R.drawable.ic_launcher_background)
                   .error(R.drawable.ic_launcher_background)
                   .into(ivMoviePoster);

           // Load backdrop (same image but with reduced opacity handled by layout)
           Glide.with(this)
                   .load(fullPosterUrl)
                   .placeholder(R.drawable.ic_launcher_background)
                   .error(R.drawable.ic_launcher_background)
                   .into(ivMovieBackdrop);
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