package com.example.moviemax.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moviemax.Adapter.DateAdapter;
import com.example.moviemax.Adapter.ShowTimeAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.MovieApi;
import com.example.moviemax.Api.ShowTimeApi;

import com.example.moviemax.Model.DateItemModel.DateItem;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moviemax.Adapter.DateAdapter;
import com.example.moviemax.Adapter.ShowTimeAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.ShowTimeApi;
import com.example.moviemax.Model.DateItemModel.DateItem;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowTimeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView dateRecyclerView;
    ShowTimeAdapter adapter;
    DateAdapter dateAdapter;
    List<ShowTimeResponse> showTimeList = new ArrayList<>();
    List<ShowTimeResponse> allShowTimes = new ArrayList<>();
    private String selectedDate = "";
    private int movieId = -1; // ID phim được truyền từ MainActivity (nếu có)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);

        // Nhận movieId từ Intent (nếu có)
        Intent intent = getIntent();
        if (intent.hasExtra("MOVIE_ID")) {
            movieId = intent.getIntExtra("MOVIE_ID", -1);
            Log.d("ShowTimeActivity", "Received Movie ID: " + movieId);
        }

        // Initialize RecyclerViews
        dateRecyclerView = findViewById(R.id.dateRecyclerView);
        recyclerView = findViewById(R.id.recyclerView);

        // Setup Date Carousel
        setupDateCarousel();

        // Setup Showtime RecyclerView với click listener
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowTimeAdapter(this, showTimeList, showtime -> {
            // Khi click vào showtime (hoặc nút time), chuyển sang BookingActivity
            Intent bookingIntent = new Intent(ShowTimeActivity.this, BookingActivity.class);
            bookingIntent.putExtra("SHOWTIME_ID", (long) showtime.getId());
            bookingIntent.putExtra("MOVIE_TITLE", showtime.getMovieTitle());
            bookingIntent.putExtra("CINEMA_NAME", showtime.getCinemaName());
            bookingIntent.putExtra("ROOM_NAME", showtime.getRoomName());
            bookingIntent.putExtra("START_TIME", showtime.getStartTime());
            bookingIntent.putExtra("PRICE", showtime.getPrice());
            startActivity(bookingIntent);

            Log.d("ShowTimeActivity", "Navigate to BookingActivity with showtime ID: " + showtime.getId());
        });
        recyclerView.setAdapter(adapter);

        // Load data from API
        loadShowTimes();
    }

    private void setupDateCarousel() {
        // Generate dates
        List<DateItem> dates = generateDates();
        Log.d("ShowTimeActivity", "Generated " + dates.size() + " dates");

        // Setup Date Adapter
        dateAdapter = new DateAdapter(dates, position -> {
            // Update selected date
            for (int i = 0; i < dates.size(); i++) {
                dates.get(i).setSelected(i == position);
            }
            dateAdapter.notifyDataSetChanged();

            // Save selected date
            selectedDate = dates.get(position).getFullDate();

            // Filter showtimes by date
            filterShowTimesByDate(selectedDate);

            Log.d("ShowTimeActivity", "Selected date: " + selectedDate);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        dateRecyclerView.setLayoutManager(layoutManager);
        dateRecyclerView.setAdapter(dateAdapter);

        Log.d("ShowTimeActivity", "Date carousel setup completed");
    }

    private List<DateItem> generateDates() {
        List<DateItem> datesList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 14; i++) {
            String day = dayFormat.format(calendar.getTime());
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            String fullDate = fullDateFormat.format(calendar.getTime());

            DateItem dateItem = new DateItem(day, date, fullDate);
            if (i == 0) {
                dateItem.setSelected(true);
                selectedDate = fullDate;
            }
            datesList.add(dateItem);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return datesList;
    }

    private void loadShowTimes() {
        ShowTimeApi api = ApiService.getClient().create(ShowTimeApi.class);

        api.getShowTimes().enqueue(new Callback<List<ShowTimeResponse>>() {
            @Override
            public void onResponse(Call<List<ShowTimeResponse>> call, Response<List<ShowTimeResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());

                if (!response.isSuccessful()) {
                    try {
                        Log.e("API_RESPONSE_ERROR", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if (response.body() != null) {
                    allShowTimes.clear();
                    allShowTimes.addAll(response.body());

                    // Nếu có movieId, filter theo movie title
                    if (movieId != -1) {
                        MovieApi movieApi = ApiService.getClient().create(MovieApi.class);

                        movieApi.getMovieById(movieId).enqueue(new Callback<MovieResponse>() {
                            @Override
                            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> movieResponse) {
                                if (movieResponse.isSuccessful() && movieResponse.body() != null) {
                                    String movieTitle = movieResponse.body().getTitle();
                                    Log.d("MovieFilter", "Movie title: " + movieTitle);

                                    List<ShowTimeResponse> filteredByMovie = new ArrayList<>();
                                    for (ShowTimeResponse st : allShowTimes) {
                                        if (st.getMovieTitle() != null && st.getMovieTitle().equalsIgnoreCase(movieTitle)) {
                                            filteredByMovie.add(st);
                                        }
                                    }

                                    allShowTimes.clear();
                                    allShowTimes.addAll(filteredByMovie);

                                    // Sau khi lọc xong => filter theo ngày
                                    filterShowTimesByDate(selectedDate);
                                    Log.d("ShowTimeActivity", "Filtered showtimes count: " + allShowTimes.size());
                                }
                            }

                            @Override
                            public void onFailure(Call<MovieResponse> call, Throwable t) {
                                Log.e("MovieApiError", t.getMessage(), t);
                            }
                        });
                    } else {
                        // Nếu không có movieId thì chỉ lọc theo ngày
                        filterShowTimesByDate(selectedDate);
                        Log.d("ShowTimeActivity", "Loaded " + allShowTimes.size() + " showtimes (no filter)");
                    }
                } else {
                    Toast.makeText(ShowTimeActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShowTimeResponse>> call, Throwable t) {
                t.printStackTrace();
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(ShowTimeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterShowTimesByDate(String date) {
        if (allShowTimes == null || allShowTimes.isEmpty()) {
            Log.d("ShowTimeActivity", "No showtimes to filter");
            return;
        }

        showTimeList.clear();

        for (ShowTimeResponse showTime : allShowTimes) {
            try {
                // Parse startTime from API (format: "2025-10-19T18:00:00")
                String startTime = showTime.getStartTime();

                // Extract date part from startTime
                String showtimeDate = startTime.substring(0, 10); // Get "2025-10-19"

                // Compare with selected date
                if (showtimeDate.equals(date)) {
                    showTimeList.add(showTime);
                }

            } catch (Exception e) {
                Log.e("ShowTimeActivity", "Error parsing date: " + e.getMessage());
            }
        }

        adapter.notifyDataSetChanged();

        Log.d("ShowTimeActivity", "Filtered " + showTimeList.size() + " showtimes for date: " + date);

        // Show toast if no showtimes found
        if (showTimeList.isEmpty()) {
            Toast.makeText(this,
                    "Không có suất chiếu nào trong ngày này",
                    Toast.LENGTH_SHORT).show();
        }
    }
}