package com.example.moviemax.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.SeatApi;
import com.example.moviemax.Model.SeatDto.SeatResponse;
import com.example.moviemax.R;
import com.example.moviemax.Utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private GridLayout seatGrid;
    private TextView txtMovieTitle, txtCinemaInfo;
    private TextView txtSelectedSeats, txtTotalPrice;
    private Button btnBook;

    private List<SeatResponse> seats = new ArrayList<>();
    private List<SeatResponse> selectedSeats = new ArrayList<>();

    // Store original seat colors to restore on deselection
    private List<String> originalSeatStatuses = new ArrayList<>();

    private long showtimeId;
    private long accountId;
    private double seatPrice;
    private SessionManager sessionManager;

    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private String startTime;
    
    // New flow variables
    private boolean isNewFlow = false;
    private ArrayList<?> selectedFoodItems;
    private double foodTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_ticket);

        // Initialize views
        seatGrid = findViewById(R.id.seatGrid);
        txtMovieTitle = findViewById(R.id.txtMovieTitle);
        txtCinemaInfo = findViewById(R.id.txtCinemaInfo);
        txtSelectedSeats = findViewById(R.id.txtSelectedSeats);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnBook = findViewById(R.id.btnBook);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Get data from Intent
        showtimeId = getIntent().getLongExtra("SHOWTIME_ID", -1);
        movieTitle = getIntent().getStringExtra("MOVIE_TITLE");
        cinemaName = getIntent().getStringExtra("CINEMA_NAME");
        roomName = getIntent().getStringExtra("ROOM_NAME");
        startTime = getIntent().getStringExtra("START_TIME");
        seatPrice = getIntent().getDoubleExtra("PRICE", 50000);

        // Check if this is new flow (Food -> Seat -> Payment)
        isNewFlow = getIntent().getBooleanExtra("NEW_FLOW", false);
        if (isNewFlow) {
            // Get account ID from intent (new flow)
            accountId = getIntent().getLongExtra("ACCOUNT_ID", -1);
            selectedFoodItems = (ArrayList<?>) getIntent().getSerializableExtra("SELECTED_FOOD_ITEMS");
            foodTotal = getIntent().getDoubleExtra("FOOD_TOTAL", 0.0);
            
            Log.d("BookingActivity", "New flow detected with food items: " + 
                  (selectedFoodItems != null ? selectedFoodItems.size() : 0));
        } else {
            // Get accountId from SessionManager (old flow)
            accountId = sessionManager.getAccountId();
        }

        // Display information
        txtMovieTitle.setText(movieTitle != null ? movieTitle : "Movie Title");
        String formattedTime = formatTime(startTime);
        txtCinemaInfo.setText(cinemaName + " " + roomName + " • " + formattedTime);

        // Disable button initially
        btnBook.setEnabled(false);

        // Load seats from API
        loadSeats();

        // Set button click based on flow
        if (isNewFlow) {
            btnBook.setOnClickListener(v -> goToPayment());
        } else {
            btnBook.setOnClickListener(v -> goToFoodSelection());
        }
    }

    private String formatTime(String startTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(startTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("BookingActivity", "Error formatting time: " + e.getMessage());
            return startTime != null ? startTime : "";
        }
    }

    private void loadSeats() {
        SeatApi api = ApiService.getClient().create(SeatApi.class);

        api.getSeatsByShowtime(showtimeId).enqueue(new Callback<List<SeatResponse>>() {
            @Override
            public void onResponse(Call<List<SeatResponse>> call, Response<List<SeatResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    seats.clear();
                    originalSeatStatuses.clear();

                    seats.addAll(response.body());

                    // Store original statuses
                    for (SeatResponse seat : seats) {
                        originalSeatStatuses.add(seat.getStatus());
                    }

                    displaySeats();
                    Log.d("BookingActivity", "Loaded " + seats.size() + " seats");
                } else {
                    Toast.makeText(BookingActivity.this,
                            "Không thể tải danh sách ghế",
                            Toast.LENGTH_SHORT).show();
                    try {
                        if (response.errorBody() != null) {
                            Log.e("API_ERROR", "Error: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SeatResponse>> call, Throwable t) {
                Log.e("API_ERROR", "Load seats failed: " + t.getMessage(), t);
                Toast.makeText(BookingActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySeats() {
        seatGrid.removeAllViews();

        for (int i = 0; i < seats.size(); i++) {
            SeatResponse seat = seats.get(i);
            final int seatIndex = i;

            TextView seatView = new TextView(this);

            // Display seat number
            String displayText = seat.getSeatNumber();
            if (displayText != null && displayText.length() > 1) {
                displayText = displayText.substring(1);
            }

            seatView.setText(displayText);
            seatView.setTextColor(Color.WHITE);
            seatView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            seatView.setPadding(12, 12, 12, 12);
            seatView.setTextSize(12);

            // Set color by status - use TEMPORARY_SELECTED for local selection
            int bgColor = getSeatColor(seat.getStatus(), isSelectedLocally(seat));
            seatView.setBackgroundColor(bgColor);

            // Set layout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 80;
            params.height = 80;
            params.setMargins(4, 4, 4, 4);
            seatView.setLayoutParams(params);

            // Only allow click if seat is AVAILABLE
            String status = seat.getStatus() != null ? seat.getStatus().toUpperCase() : "";
            if (status.equals("AVAILABLE")) {
                seatView.setOnClickListener(v -> onSeatClicked(seat, seatView, seatIndex));
            }

            seatGrid.addView(seatView);
        }
    }

    private boolean isSelectedLocally(SeatResponse seat) {
        for (SeatResponse selected : selectedSeats) {
            if (selected.getId() == seat.getId()) {
                return true;
            }
        }
        return false;
    }

    private int getSeatColor(String status, boolean isSelectedLocally) {
        if (status == null) return Color.parseColor("#4CAF50");

        // If locally selected, show red
        if (isSelectedLocally) {
            return Color.parseColor("#D32F2F");  // Red
        }

        String upperStatus = status.toUpperCase();
        switch (upperStatus) {
            case "BOOKED":
            case "RESERVED":
                return Color.GRAY;  // Gray - cannot select
            case "AVAILABLE":
            default:
                return Color.parseColor("#4CAF50");  // Green - available
        }
    }

    private void onSeatClicked(SeatResponse seat, TextView view, int seatIndex) {
        String status = seat.getStatus() != null ? seat.getStatus().toUpperCase() : "";

        // Don't allow clicking on booked/reserved seats
        if (status.equals("BOOKED") || status.equals("RESERVED")) {
            return;
        }

        // Toggle local selection WITHOUT changing database
        if (isSelectedLocally(seat)) {
            // Remove from selected list
            selectedSeats.removeIf(s -> s.getId() == seat.getId());
        } else {
            // Add to selected list
            selectedSeats.add(seat);
        }

        // Update color based on local selection
        view.setBackgroundColor(getSeatColor(seat.getStatus(), isSelectedLocally(seat)));

        // Update summary
        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            txtSelectedSeats.setText("Chưa chọn ghế");
            txtTotalPrice.setText("0 VNĐ");
            btnBook.setText("Chọn ghế");
            btnBook.setEnabled(false);
        } else {
            // Display selected seats
            StringBuilder sb = new StringBuilder();
            for (SeatResponse s : selectedSeats) {
                sb.append(s.getSeatNumber()).append(" ");
            }
            txtSelectedSeats.setText("Ghế đã chọn: " + sb.toString().trim());

            // Calculate total
            double total = selectedSeats.size() * seatPrice;
            txtTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", total));

            // Update button
            btnBook.setText("Tiếp tục (" + selectedSeats.size() + " ghế)");
            btnBook.setEnabled(true);
        }
    }

    private void goToFoodSelection() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (accountId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create list of seat IDs
        ArrayList<Long> seatIds = new ArrayList<>();
        for (SeatResponse seat : selectedSeats) {
            seatIds.add((long) seat.getId());
        }

        // Navigate to FoodActivity with full information
        Intent intent = new Intent(BookingActivity.this, FoodActivity.class);
        intent.putExtra("ACCOUNT_ID", accountId);
        intent.putExtra("SHOWTIME_ID", showtimeId);
        intent.putExtra("SEAT_IDS", seatIds);
        intent.putExtra("SEAT_PRICE", seatPrice);
        intent.putExtra("MOVIE_TITLE", movieTitle);
        intent.putExtra("CINEMA_NAME", cinemaName);
        intent.putExtra("ROOM_NAME", roomName);
        intent.putExtra("START_TIME", startTime);

        startActivity(intent);
        finish();
    }

    private void goToPayment() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (accountId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create list of seat IDs for new flow
        ArrayList<Long> seatIds = new ArrayList<>();
        for (SeatResponse seat : selectedSeats) {
            seatIds.add((long) seat.getId());
        }

        // Calculate totals for the new flow
        int seatCount = selectedSeats.size();
        double seatTotal = seatCount * seatPrice;
        double totalAmount = seatTotal + foodTotal;
        
        // Generate seat numbers string
        StringBuilder seatNumbers = new StringBuilder();
        for (int i = 0; i < selectedSeats.size(); i++) {
            seatNumbers.append(selectedSeats.get(i).getSeatNumber());
            if (i < selectedSeats.size() - 1) {
                seatNumbers.append(", ");
            }
        }
        
        // Navigate to PaymentActivity with all data (new flow)
        Intent intent = new Intent(BookingActivity.this, PaymentActivity.class);
        intent.putExtra("ACCOUNT_ID", accountId);
        intent.putExtra("SHOWTIME_ID", showtimeId);
        intent.putExtra("SEAT_IDS", seatIds);
        intent.putExtra("SEAT_PRICE", seatPrice);
        intent.putExtra("SEAT_COUNT", seatCount);
        intent.putExtra("SEAT_NUMBERS", seatNumbers.toString());
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("MOVIE_TITLE", movieTitle);
        intent.putExtra("CINEMA_NAME", cinemaName);
        intent.putExtra("ROOM_NAME", roomName);
        intent.putExtra("START_TIME", startTime);
        
        // Pass food data for new flow
        intent.putExtra("SELECTED_FOOD_ITEMS", selectedFoodItems);
        intent.putExtra("FOOD_TOTAL", foodTotal);
        intent.putExtra("NEW_FLOW", true);

        Log.d("BookingActivity", "Going to PaymentActivity (new flow) with:");
        Log.d("BookingActivity", "- Seats: " + selectedSeats.size());
        Log.d("BookingActivity", "- Seat Count: " + seatCount);
        Log.d("BookingActivity", "- Seat Numbers: " + seatNumbers.toString());
        Log.d("BookingActivity", "- Seat Total: " + seatTotal);
        Log.d("BookingActivity", "- Food items: " + (selectedFoodItems != null ? selectedFoodItems.size() : 0));
        Log.d("BookingActivity", "- Food total: " + foodTotal);
        Log.d("BookingActivity", "- Total Amount: " + totalAmount);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload seats when returning to this activity to get fresh status
        loadSeats();
    }
}