package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< Updated upstream
=======
import android.util.Log;
import android.widget.Button;
>>>>>>> Stashed changes
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviemax.Adapter.MovieAdapter;
import com.example.moviemax.Adapter.SimpleShowtimeAdapter;
import com.example.moviemax.R;
<<<<<<< Updated upstream
=======
import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// import jp.wasabeef.glide.transformations.BlurTransformation;
>>>>>>> Stashed changes

public class DetailsActivity extends AppCompatActivity {
    private ImageView ivMoviePoster, ivMovieBackdrop;
    private TextView tvMovieTitle, tvMovieGenre, tvMovieDuration, tvMovieLanguage;
    private TextView tvMovieDirector, tvMovieCast, tvMovieReleaseDate, tvMovieDescription, tvMovieRating;
    private TextView tvMovieGenreAndDuration;
    private ImageButton btnBack;
<<<<<<< Updated upstream
=======
    private Button btnPlay, btnBookTickets, btnWishlist;
    private RecyclerView recyclerViewShowtimes, recyclerViewSimilarMovies;
    private SimpleShowtimeAdapter showtimeAdapter;
    private MovieAdapter similarMoviesAdapter;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        loadMovieDetails();
        loadShowtimes();
        loadSimilarMovies();
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
<<<<<<< Updated upstream
=======
        btnPlay = findViewById(R.id.btnPlay);
        btnBookTickets = findViewById(R.id.btnBookTickets);
        btnWishlist = findViewById(R.id.btnWishlist);
        
        // RecyclerViews
        recyclerViewShowtimes = findViewById(R.id.recyclerViewShowtimes);
        recyclerViewSimilarMovies = findViewById(R.id.recyclerViewSimilarMovies);
    }

    private void setupRecyclerViews() {
        // Showtimes RecyclerView
        showtimeAdapter = new SimpleShowtimeAdapter(this, new ArrayList<>());
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewShowtimes.setAdapter(showtimeAdapter);
        
        // Similar Movies RecyclerView
        similarMoviesAdapter = new MovieAdapter(this, new ArrayList<>());
        recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSimilarMovies.setAdapter(similarMoviesAdapter);
>>>>>>> Stashed changes
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
<<<<<<< Updated upstream
=======
        
        btnPlay.setOnClickListener(v -> {
            // Placeholder for play functionality
            Toast.makeText(this, "Play functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnBookTickets.setOnClickListener(v -> {
            // Navigate to ShowTimeActivity or booking functionality
            Intent intent = new Intent(DetailsActivity.this, ShowTimeActivity.class);
            // Pass movie data if needed
            startActivity(intent);
        });
        
        btnWishlist.setOnClickListener(v -> {
            // Toggle wishlist functionality
            Toast.makeText(this, "Added to Wishlist!", Toast.LENGTH_SHORT).show();
        });
>>>>>>> Stashed changes
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

        // Load poster image
        if (posterUrl != null && !posterUrl.isEmpty()) {
<<<<<<< Updated upstream
            String fullPosterUrl = "http://103.200.20.174:8081/images/" + posterUrl;
=======
            String fullPosterUrl = getFullPosterUrl(posterUrl);
            
            // Load main poster
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
}
=======

    private String getFullPosterUrl(String posterUrl) {
        Log.d("getFullPosterUrl", posterUrl);
        if (posterUrl.startsWith("http")) {
            // Already a full URL (Supabase full URL)
            return posterUrl;
        } else if (posterUrl.startsWith("poster_")) {
            // Supabase storage filename
            return SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
        } else {
            // Backend image path
            return "http://103.200.20.174:8081/images/" + posterUrl;
        }
    }
    
    private void loadShowtimes() {
        // Sample showtime data - in a real app, this would come from API
        List<SimpleShowtimeAdapter.ShowtimeData> showtimes = Arrays.asList(
            new SimpleShowtimeAdapter.ShowtimeData("10:30 AM", "Screen 1", "$12.99"),
            new SimpleShowtimeAdapter.ShowtimeData("1:15 PM", "Screen 2", "$14.99"),
            new SimpleShowtimeAdapter.ShowtimeData("4:00 PM", "IMAX", "$18.99"),
            new SimpleShowtimeAdapter.ShowtimeData("7:30 PM", "Screen 1", "$16.99"),
            new SimpleShowtimeAdapter.ShowtimeData("10:15 PM", "Screen 3", "$14.99")
        );
        
        showtimeAdapter.updateShowtimes(showtimes);
    }
    
    private void loadSimilarMovies() {
        // In a real app, you'd call API to get similar movies
        // For now, we'll just show placeholder message
        Toast.makeText(this, "Similar movies will be loaded from API", Toast.LENGTH_SHORT).show();
    }
}
>>>>>>> Stashed changes
