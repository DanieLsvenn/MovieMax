package com.example.moviemax.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.BookingApi;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.Model.BookingDto.BookingFoodItem;
import com.example.moviemax.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity {

    private TextView tvBookingId, tvMovieName, tvSeats, tvTotal, tvCinema, tvRoom, tvStartTime, tvFood, tvBookingDate, tvStatus;
    private BookingApi bookingApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        tvBookingId = findViewById(R.id.tvBookingId);
        tvMovieName = findViewById(R.id.tvMovieName);
        tvSeats = findViewById(R.id.tvSeats);
        tvTotal = findViewById(R.id.tvTotal);
        tvCinema = findViewById(R.id.tvCinema);
        tvRoom = findViewById(R.id.tvRoom);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvFood = findViewById(R.id.tvFood);
        // Optional fields - might not exist in all layouts
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvStatus = findViewById(R.id.tvStatus);

        bookingApi = ApiService.getClient().create(BookingApi.class);

        long bookingId = getIntent().getLongExtra("BOOKING_ID", -1);
        if (bookingId == -1) {
            // Fallback to lowercase for compatibility
            bookingId = getIntent().getLongExtra("bookingId", -1);
        }
        
        if (bookingId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin vé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        loadBookingDetail(bookingId);
    }

    private void loadBookingDetail(long id) {
        Log.d("TicketDetailActivity", "Loading booking details for ID: " + id);
        Call<BookingResponse> call = bookingApi.getBookingById(id);
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    BookingResponse booking = response.body();
                    if (booking != null) {
                        displayBookingDetails(booking);
                    } else {
                        showError("Không tìm thấy thông tin đặt vé");
                    }
                } else {
                    String errorMsg = "Không thể tải thông tin vé (Mã lỗi: " + response.code() + ")";
                    Log.e("BOOKING_ERROR", "Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("BOOKING_ERROR", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("BOOKING_ERROR", "Error parsing error body", e);
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e("BOOKING_ERROR", "Network error: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayBookingDetails(BookingResponse booking) {
        Log.d("TicketDetailActivity", "Displaying booking details for: " + booking.getId());
        Log.d("TicketDetailActivity", "Movie: " + booking.getMovieTitle());
        Log.d("TicketDetailActivity", "Cinema: " + booking.getCinemaName());
        Log.d("TicketDetailActivity", "Food items count: " + (booking.getFoodItems() != null ? booking.getFoodItems().size() : 0));
        
        tvBookingId.setText("Mã đặt vé: #" + booking.getId());
        tvMovieName.setText(booking.getMovieTitle());
        tvCinema.setText(booking.getCinemaName());
        tvRoom.setText(booking.getRoomName());
        
        // Format start time
        tvStartTime.setText(formatDateTime(booking.getStartTime()));
        
        // Format booking date
        if (tvBookingDate != null && booking.getBookingDate() != null) {
            tvBookingDate.setText("Ngày đặt: " + formatDateTime(booking.getBookingDate()));
        }
        
        // Display booking status
        if (tvStatus != null) {
            String status = booking.getBookingStatus();
            String statusText = "Trạng thái: " + (status != null ? status : "Không xác định");
            
            // Set color based on status
            if ("CONFIRMED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else if ("PENDING".equalsIgnoreCase(status)) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            } else if ("CANCELLED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
            
            tvStatus.setText(statusText);
        }
        
        // Display seats
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            tvSeats.setText("Ghế: " + String.join(", ", booking.getSeats()));
        } else {
            tvSeats.setText("Ghế: Không có thông tin");
        }
        
        // Display food items with detailed information
        displayFoodItems(booking.getFoodItems());
        
        // Format total amount
        tvTotal.setText(String.format("Tổng tiền: %,.0f VNĐ", booking.getTotalAmount()));
    }

    private void displayFoodItems(List<BookingFoodItem> foodItems) {
        if (foodItems != null && !foodItems.isEmpty()) {
            StringBuilder foodText = new StringBuilder();
            double foodTotal = 0;
            
            for (int i = 0; i < foodItems.size(); i++) {
                BookingFoodItem item = foodItems.get(i);
                foodText.append(String.format("%s x%d = %,.0f đ", 
                    item.getFoodName(), 
                    item.getQuantity(), 
                    item.getSubtotal()));
                
                foodTotal += item.getSubtotal();
                
                if (i < foodItems.size() - 1) {
                    foodText.append("\n");
                }
            }
            
            foodText.append(String.format("\nTổng đồ ăn: %,.0f đ", foodTotal));
            tvFood.setText(foodText.toString());
        } else {
            tvFood.setText("Đồ ăn & nước uống: Không có");
        }
    }

    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return "Không có thông tin";
        }
        
        try {
            // Try different date formats that might come from the API
            String[] inputFormats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
            };
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            
            for (String inputFormat : inputFormats) {
                try {
                    SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat, Locale.getDefault());
                    Date date = inputFormatter.parse(dateTimeStr);
                    return outputFormat.format(date);
                } catch (Exception e) {
                    // Continue to next format
                }
            }
            
            // If no format worked, return the original string
            return dateTimeStr;
        } catch (Exception e) {
            Log.w("TicketDetail", "Error formatting date: " + dateTimeStr, e);
            return dateTimeStr;
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("TicketDetailActivity", message);
    }
}