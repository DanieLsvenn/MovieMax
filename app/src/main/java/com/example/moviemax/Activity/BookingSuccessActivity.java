package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.MainActivity;
import com.example.moviemax.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingSuccessActivity extends AppCompatActivity {

    private TextView tvBookingId, tvMovieTitle, tvCinemaInfo, tvSeatCount, tvTotalAmount;
    private Button btnBackToHome, btnViewTicket;

    private long bookingId;
    private String movieTitle, cinemaName, roomName, startTime;
    private int seatCount;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        initViews();
        getIntentData();
        displayBookingInfo();
        setupListeners();
    }

    private void initViews() {
        tvBookingId = findViewById(R.id.tvBookingId);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCinemaInfo = findViewById(R.id.tvCinemaInfo);
        tvSeatCount = findViewById(R.id.tvSeatCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnViewTicket = findViewById(R.id.btnViewTicket);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        bookingId = intent.getLongExtra("BOOKING_ID", -1);
        movieTitle = intent.getStringExtra("MOVIE_TITLE");
        cinemaName = intent.getStringExtra("CINEMA_NAME");
        roomName = intent.getStringExtra("ROOM_NAME");
        startTime = intent.getStringExtra("START_TIME");
        seatCount = intent.getIntExtra("SEAT_COUNT", 0);
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0);
    }

    private void displayBookingInfo() {
        tvBookingId.setText("Mã đặt vé: #" + bookingId);
        tvMovieTitle.setText(movieTitle != null ? movieTitle : "Movie Title");

        String formattedTime = formatTime(startTime);
        tvCinemaInfo.setText(cinemaName + " " + roomName + " • " + formattedTime);

        tvSeatCount.setText(seatCount + " ghế");
        tvTotalAmount.setText(formatPrice((int) totalAmount));
    }

    private void setupListeners() {
        btnBackToHome.setOnClickListener(v -> {
            // Quay về màn hình chính
            Intent intent = new Intent(BookingSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnViewTicket.setOnClickListener(v -> {
            // Chuyển đến màn hình xem vé
            Intent intent = new Intent(BookingSuccessActivity.this, TicketDetailActivity.class);
            intent.putExtra("BOOKING_ID", bookingId);
            startActivity(intent);
        });
    }

    private String formatTime(String startTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(startTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            return startTime != null ? startTime : "";
        }
    }

    private String formatPrice(int price) {
        return String.format(Locale.getDefault(), "%,dđ", price);
    }

//    @Override
//    public void onBackPressed() {
//        // Không cho phép back
//        btnBackToHome.performClick();
//    }
}