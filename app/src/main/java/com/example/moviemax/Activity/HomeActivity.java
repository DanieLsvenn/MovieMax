package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.BannerAdapter;
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
    private RecyclerView recyclerViewMovies, recyclerViewSearchResults, recyclerViewBanners, 
                         recyclerViewComingSoon, recyclerViewPremium;
    private MovieAdapter movieAdapter, searchResultsAdapter, comingSoonAdapter, premiumAdapter;
    private BannerAdapter bannerAdapter;
    private ProgressBar progressBar;
    private TextView tvError, tvSearchResultsTitle, tvNoResults;
    private List<MovieResponse> movieList, searchResultsList, comingSoonList, premiumList, featuredList;
    
    // Search UI components
    private ImageView btnSearch, btnClearSearch, btnProfile;
    private LinearLayout searchContainer, moviesContainer, searchResultsContainer;
    private EditText etSearch;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    // Booking components
    private Button btnQuickBooking;
    private TextView tvSeeAllNowPlaying, tvSeeAllComingSoon, tvSeeAllPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        initViews();
        setupRecyclerViews();
        setupSearchFunctionality();
        setupBookingButtons();
        loadMovies();
    }

    private void initViews() {
        // RecyclerViews
        recyclerViewMovies = findViewById(R.id.recyclerViewMovies);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        recyclerViewBanners = findViewById(R.id.recyclerViewBanners);
        recyclerViewComingSoon = findViewById(R.id.recyclerViewComingSoon);
        recyclerViewPremium = findViewById(R.id.recyclerViewPremium);
        
        // Progress and Error
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvSearchResultsTitle = findViewById(R.id.tvSearchResultsTitle);
        tvNoResults = findViewById(R.id.tvNoResults);
        
        // Search components
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnProfile = findViewById(R.id.btnProfile);
        searchContainer = findViewById(R.id.searchContainer);
        moviesContainer = findViewById(R.id.moviesContainer);
        searchResultsContainer = findViewById(R.id.searchResultsContainer);
        etSearch = findViewById(R.id.etSearch);
        
        // Booking components
        btnQuickBooking = findViewById(R.id.btnQuickBooking);
        tvSeeAllNowPlaying = findViewById(R.id.tvSeeAllNowPlaying);
        tvSeeAllComingSoon = findViewById(R.id.tvSeeAllComingSoon);
        tvSeeAllPremium = findViewById(R.id.tvSeeAllPremium);
        
        // Data lists
        movieList = new ArrayList<>();
        searchResultsList = new ArrayList<>();
        comingSoonList = new ArrayList<>();
        premiumList = new ArrayList<>();
        featuredList = new ArrayList<>();
        
        // Search handler
        searchHandler = new Handler();
    }

    private void setupRecyclerViews() {
        // Featured banners RecyclerView (horizontal)
        bannerAdapter = new BannerAdapter(this, featuredList);
        recyclerViewBanners.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewBanners.setAdapter(bannerAdapter);
        
        // Now Playing movies RecyclerView (horizontal)
        movieAdapter = new MovieAdapter(this, movieList);
        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMovies.setAdapter(movieAdapter);
        
        // Coming Soon RecyclerView (horizontal)
        comingSoonAdapter = new MovieAdapter(this, comingSoonList);
        recyclerViewComingSoon.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewComingSoon.setAdapter(comingSoonAdapter);
        
        // Premium RecyclerView (horizontal)
        premiumAdapter = new MovieAdapter(this, premiumList);
        recyclerViewPremium.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPremium.setAdapter(premiumAdapter);
        
        // Search results RecyclerView (horizontal)
        searchResultsAdapter = new MovieAdapter(this, searchResultsList);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSearchResults.setAdapter(searchResultsAdapter);
    }
    
    private void setupSearchFunctionality() {
        // Search button click
        btnSearch.setOnClickListener(v -> toggleSearchBar());
        
        // Profile button click
        btnProfile.setOnClickListener(v -> openProfile());
        
        // Clear search button click
        btnClearSearch.setOnClickListener(v -> clearSearch());
        
        // Search EditText functionality
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
        
        // Text change listener for real-time search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showMoviesSection();
                } else {
                    // Debounce search - wait 500ms after user stops typing
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupBookingButtons() {
        // Quick Booking button
        btnQuickBooking.setOnClickListener(v -> {
            Toast.makeText(this, "Browse all movies functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        // See All buttons
        tvSeeAllNowPlaying.setOnClickListener(v -> {
            Toast.makeText(this, "See all now playing movies", Toast.LENGTH_SHORT).show();
        });
        
        tvSeeAllComingSoon.setOnClickListener(v -> {
            Toast.makeText(this, "See all coming soon movies", Toast.LENGTH_SHORT).show();
        });
        
        tvSeeAllPremium.setOnClickListener(v -> {
            Toast.makeText(this, "See all premium movies", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void toggleSearchBar() {
        if (searchContainer.getVisibility() == View.GONE) {
            searchContainer.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        } else {
            clearSearch();
        }
    }
    
    private void clearSearch() {
        etSearch.setText("");
        searchContainer.setVisibility(View.GONE);
        showMoviesSection();
    }
    
    private void showMoviesSection() {
        moviesContainer.setVisibility(View.VISIBLE);
        searchResultsContainer.setVisibility(View.GONE);
    }
    
    private void showSearchResults() {
        moviesContainer.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        moviesContainer.setVisibility(View.GONE);
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
                    
                    List<MovieResponse> allMovies = response.body();
                    
                    // Populate different sections
                    populateMovieSections(allMovies);
                    
                    moviesContainer.setVisibility(View.VISIBLE);
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
    
    private void performSearch(String query) {
        Log.d("SEARCH", "Searching for: " + query);
        
        // Show search results section
        showSearchResults();
        tvNoResults.setVisibility(View.GONE);
        
        // Filter from existing movies first (instant results)
        List<MovieResponse> filteredMovies = new ArrayList<>();
        for (MovieResponse movie : movieList) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredMovies.add(movie);
            }
        }
        
        // Update search results
        searchResultsList.clear();
        searchResultsList.addAll(filteredMovies);
        searchResultsAdapter.updateMovies(searchResultsList);
        tvSearchResultsTitle.setText("Search Results for \"" + query + "\"");
        
        if (filteredMovies.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("No movies found for \"" + query + "\"");
        }
        
        // Optionally, you can also call API search if you want server-side search
        // searchMoviesFromAPI(query);
    }
    
    private void searchMoviesFromAPI(String query) {
        Call<List<MovieResponse>> call = ApiService.getMovieApiService().searchMovies(query);
        call.enqueue(new Callback<List<MovieResponse>>() {
            @Override
            public void onResponse(Call<List<MovieResponse>> call, Response<List<MovieResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SEARCH_API", "Search results: " + response.body().size() + " movies");
                    searchResultsList.clear();
                    searchResultsList.addAll(response.body());
                    searchResultsAdapter.updateMovies(searchResultsList);
                    
                    if (response.body().isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        tvNoResults.setText("No movies found for \"" + query + "\"");
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("SEARCH_API_ERROR", "Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MovieResponse>> call, Throwable t) {
                Log.e("SEARCH_API_ERROR", "Search API Error: " + t.getMessage(), t);
            }
        });
    }

    private void populateMovieSections(List<MovieResponse> allMovies) {
        // Clear all lists
        movieList.clear();
        featuredList.clear();
        comingSoonList.clear();
        premiumList.clear();
        
        // Sort movies by rating to get featured movies
        List<MovieResponse> sortedByRating = new ArrayList<>(allMovies);
        sortedByRating.sort((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()));
        
        // Populate sections
        for (int i = 0; i < allMovies.size(); i++) {
            MovieResponse movie = allMovies.get(i);
            
            // Featured Movies (top rated movies for banners)
            if (i < 3 && movie.getRating() >= 7.0) {
                featuredList.add(movie);
            }
            
            // Now Playing (all movies)
            movieList.add(movie);
            
            // Coming Soon (simulate with some movies - in real app, you'd have a release date check)
            if (i % 3 == 0) {
                comingSoonList.add(movie);
            }
            
            // Premium Movies (high-rated movies)
            if (movie.getRating() >= 8.0) {
                premiumList.add(movie);
            }
        }
        
        // Update adapters
        movieAdapter.updateMovies(movieList);
        bannerAdapter.updateMovies(featuredList);
        comingSoonAdapter.updateMovies(comingSoonList);
        premiumAdapter.updateMovies(premiumList);
        
        Log.d("MOVIE_SECTIONS", String.format(
            "Populated sections - Featured: %d, Now Playing: %d, Coming Soon: %d, Premium: %d",
            featuredList.size(), movieList.size(), comingSoonList.size(), premiumList.size()
        ));
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void openProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}
