package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.FoodAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.BookingApi;
import com.example.moviemax.Api.FoodApi;
import com.example.moviemax.Model.BookingDto.BookingRequest;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.Model.FoodDto.FoodItemRequest;
import com.example.moviemax.Model.FoodDto.FoodItemsResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodActivity extends AppCompatActivity {

    private RecyclerView rvFoods;
    private TextView tvSeatTotal, tvFoodTotal, tvGrandTotal, tvSkipFood;
    private Button btnConfirmFoods;
    private ImageButton btnBack;

    private FoodAdapter foodAdapter;
    private List<FoodItemsResponse> foodList = new ArrayList<>();

    // Dữ liệu từ BookingActivity
    private long accountId;
    private long showtimeId;
    private ArrayList<Long> seatIds;
    private double seatPrice;
    private String movieTitle, cinemaName, roomName, startTime;

    private double seatTotal = 0;
    private int foodTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        initViews();
        getIntentData();
        calculateSeatTotal();
        setupListeners();
        loadFoods();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvFoods = findViewById(R.id.rvFoods);
        tvSeatTotal = findViewById(R.id.tvSeatTotal);
        tvFoodTotal = findViewById(R.id.tvFoodTotal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnConfirmFoods = findViewById(R.id.btnConfirmFoods);
        tvSkipFood = findViewById(R.id.tvSkipFood);

        rvFoods.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIntentData() {
        Intent intent = getIntent();
        accountId = intent.getLongExtra("ACCOUNT_ID", -1);
        showtimeId = intent.getLongExtra("SHOWTIME_ID", -1);
        seatIds = (ArrayList<Long>) intent.getSerializableExtra("SEAT_IDS");
        seatPrice = intent.getDoubleExtra("SEAT_PRICE", 50000);
        movieTitle = intent.getStringExtra("MOVIE_TITLE");
        cinemaName = intent.getStringExtra("CINEMA_NAME");
        roomName = intent.getStringExtra("ROOM_NAME");
        startTime = intent.getStringExtra("START_TIME");

        Log.d("FoodActivity", "AccountId: " + accountId);
        Log.d("FoodActivity", "ShowtimeId: " + showtimeId);
        Log.d("FoodActivity", "SeatIds: " + seatIds + " (count: " + (seatIds != null ? seatIds.size() : 0) + ")");
        Log.d("FoodActivity", "SeatPrice: " + seatPrice);
    }

    private void calculateSeatTotal() {
        if (seatIds != null) {
            seatTotal = seatIds.size() * seatPrice;
            tvSeatTotal.setText(formatPrice(seatTotal));
            updateGrandTotal();

            Log.d("PRICE_DEBUG", "Seat total calculated: " + seatTotal +
                    " (" + seatIds.size() + " seats × " + seatPrice + ")");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnConfirmFoods.setOnClickListener(v -> {
            if (validateData()) {
                createBooking();
            }
        });

        tvSkipFood.setOnClickListener(v -> {
            if (validateData()) {
                createBooking();
            }
        });
    }

    private void loadFoods() {
        FoodApi api = ApiService.getClient().create(FoodApi.class);

        api.getFoods().enqueue(new Callback<List<FoodItemsResponse>>() {
            @Override
            public void onResponse(Call<List<FoodItemsResponse>> call, Response<List<FoodItemsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodList.clear();
                    foodList.addAll(response.body());

                    foodAdapter = new FoodAdapter(foodList, total -> {
                        foodTotal = total;
                        tvFoodTotal.setText(formatPrice(foodTotal));
                        updateGrandTotal();

                        Log.d("PRICE_DEBUG", "Food total updated: " + foodTotal);
                    });
                    rvFoods.setAdapter(foodAdapter);
                    Log.d("FoodActivity", "Loaded " + foodList.size() + " foods");
                } else {
                    Toast.makeText(FoodActivity.this, "Không thể tải danh sách đồ ăn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FoodItemsResponse>> call, Throwable t) {
                Log.e("FoodActivity", "Load foods failed: " + t.getMessage(), t);
                Toast.makeText(FoodActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGrandTotal() {
        double grandTotal = seatTotal + foodTotal;
        tvGrandTotal.setText(formatPrice(grandTotal));

        Log.d("PRICE_DEBUG", "=== GRAND TOTAL ===");
        Log.d("PRICE_DEBUG", "Seat: " + formatPrice(seatTotal));
        Log.d("PRICE_DEBUG", "Food: " + formatPrice(foodTotal));
        Log.d("PRICE_DEBUG", "Total: " + formatPrice(grandTotal));
        Log.d("PRICE_DEBUG", "==================");
    }

    private boolean validateData() {
        if (accountId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (showtimeId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin suất chiếu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (seatIds == null || seatIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createBooking() {
        Log.d("BOOKING_DEBUG", "=== START createBooking() ===");

        // KIỂM TRA foodAdapter
        if (foodAdapter == null) {
            Log.e("BOOKING_ERROR", "FoodAdapter is NULL!");
            Toast.makeText(this, "Lỗi: Chưa tải danh sách đồ ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        // LẤY DỮ LIỆU TỪ ADAPTER
        Map<Integer, Integer> selectedQuantities = foodAdapter.getSelectedQuantities();
        if (selectedQuantities == null) {
            Log.e("BOOKING_ERROR", "selectedQuantities is NULL!");
            selectedQuantities = new HashMap<>();
        }

        Log.d("BOOKING_DEBUG", "Selected quantities size: " + selectedQuantities.size());

        // Tạo danh sách food items
        List<FoodItemRequest> foodItems = new ArrayList<>();
        double calculatedFoodTotal = 0;

        for (Map.Entry<Integer, Integer> entry : selectedQuantities.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                FoodItemRequest item = new FoodItemRequest();
                item.setFoodId(entry.getKey());
                item.setQuantity(entry.getValue());
                foodItems.add(item);

                // Tính tổng tiền food để verify
                for (FoodItemsResponse food : foodList) {
                    if (food.getId() == entry.getKey()) {
                        double itemTotal = food.getPrice() * entry.getValue();
                        calculatedFoodTotal += itemTotal;
                        Log.d("BOOKING_DEBUG", "Food: " + food.getName() +
                                " x" + entry.getValue() + " = " + itemTotal);
                        break;
                    }
                }
            }
        }

        // Tạo booking request
        BookingRequest request = new BookingRequest();
        request.setAccountId(accountId);
        request.setShowtimeId(showtimeId);
        request.setSeatIds(seatIds);
        request.setFoodItems(foodItems.isEmpty() ? new ArrayList<>() : foodItems);

        // LOG CHI TIẾT REQUEST
        Log.d("BOOKING_REQUEST", "=== BOOKING REQUEST ===");
        Log.d("BOOKING_REQUEST", "AccountId: " + accountId);
        Log.d("BOOKING_REQUEST", "ShowtimeId: " + showtimeId);
        Log.d("BOOKING_REQUEST", "SeatIds: " + seatIds + " (count: " + seatIds.size() + ")");
        Log.d("BOOKING_REQUEST", "FoodItems count: " + foodItems.size());

        Log.d("PRICE_VERIFY", "=== PRICE VERIFICATION ===");
        Log.d("PRICE_VERIFY", "Client seat total: " + seatTotal);
        Log.d("PRICE_VERIFY", "Client food total: " + foodTotal);
        Log.d("PRICE_VERIFY", "Calculated food total: " + calculatedFoodTotal);
        Log.d("PRICE_VERIFY", "Expected grand total: " + (seatTotal + foodTotal));
        Log.d("PRICE_VERIFY", "=========================");

        // Gọi API
        BookingApi api = ApiService.getClient().create(BookingApi.class);

        api.createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                Log.d("BOOKING_DEBUG", "onResponse - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse booking = response.body();

                    Log.d("BOOKING_SUCCESS", "=== BOOKING CREATED ===");
                    Log.d("BOOKING_SUCCESS", "ID: " + booking.getId());
                    Log.d("BOOKING_SUCCESS", "Status: " + booking.getBookingStatus());
                    Log.d("BOOKING_SUCCESS", "Total Amount: " + booking.getTotalAmount());
                    Log.d("BOOKING_SUCCESS", "Seat Count: " + booking.getSeatCount());
                    Log.d("BOOKING_SUCCESS", "Food Items Count: " + booking.getFoodItemsCount());

                    // QUAN TRỌNG: So sánh giá tiền
                    double expectedTotal = seatTotal + foodTotal;
                    double actualTotal = booking.getTotalAmount();

                    Log.d("PRICE_COMPARE", "=== PRICE COMPARISON ===");
                    Log.d("PRICE_COMPARE", "Expected: " + expectedTotal);
                    Log.d("PRICE_COMPARE", "Actual: " + actualTotal);
                    Log.d("PRICE_COMPARE", "Difference: " + (actualTotal - expectedTotal));

                    if (Math.abs(actualTotal - expectedTotal) > 1) {
                        Log.w("PRICE_COMPARE", " WARNING: Price mismatch!");
                    }
                    Log.d("PRICE_COMPARE", "=======================");

                    goToPayment(booking);

                } else {
                    Log.e("BOOKING_ERROR", " Response NOT successful");
                    Log.e("BOOKING_ERROR", "Response Code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("BOOKING_ERROR", "Error Body: " + errorBody);
                        Toast.makeText(FoodActivity.this,
                                "Đặt vé thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("BOOKING_ERROR", "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e("BOOKING_ERROR", " onFailure: " + t.getMessage(), t);
                Toast.makeText(FoodActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToPayment(BookingResponse booking) {
        Log.d("BOOKING_DEBUG", "=== goToPayment() START ===");

        if (booking == null) {
            Log.e("BOOKING_ERROR", "Booking is NULL!");
            Toast.makeText(this, "Lỗi: Không có thông tin booking", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(FoodActivity.this, PaymentActivity.class);

        // Truyền thông tin booking
        intent.putExtra("BOOKING_ID", booking.getId());
        intent.putExtra("TOTAL_AMOUNT", booking.getTotalAmount());
        intent.putExtra("MOVIE_TITLE", booking.getMovieTitle());
        intent.putExtra("CINEMA_NAME", booking.getCinemaName());
        intent.putExtra("ROOM_NAME", booking.getRoomName());
        intent.putExtra("START_TIME", booking.getStartTime());
        intent.putExtra("SEAT_COUNT", booking.getSeatCount());
        intent.putExtra("SEAT_NUMBERS", booking.getSeatNumbers());
        intent.putExtra("BOOKING_STATUS", booking.getBookingStatus());

        // ✅ QUAN TRỌNG: Truyền seat IDs để update status sau thanh toán
        if (seatIds != null && !seatIds.isEmpty()) {
            ArrayList<Integer> seatIdsInt = new ArrayList<>();
            for (Long id : seatIds) {
                seatIdsInt.add(id.intValue());
            }
            intent.putIntegerArrayListExtra("SELECTED_SEAT_IDS", seatIdsInt);
            Log.d("BOOKING_DEBUG", " Passed " + seatIdsInt.size() + " seat IDs");
        } else {
            Log.w("BOOKING_DEBUG", " No seat IDs to pass!");
        }

        Log.d("BOOKING_DEBUG", "Starting PaymentActivity...");
        try {
            startActivity(intent);
            Log.d("BOOKING_DEBUG", " PaymentActivity started");
            finish();
        } catch (Exception e) {
            Log.e("BOOKING_ERROR", "Failed to start PaymentActivity", e);
            Toast.makeText(this, "Lỗi: Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0f VNĐ", price);
    }
}