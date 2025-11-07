package com.example.moviemax.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.BookingApi;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailActivity extends AppCompatActivity {

    private TextView tvBookingId, tvMovieName, tvSeats, tvTotal, tvCinema, tvRoom, tvStartTime, tvFood;
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

        bookingApi = ApiService.getClient().create(BookingApi.class);

        long bookingId = getIntent().getLongExtra("bookingId", 1);
        loadBookingDetail(bookingId);
    }

    private void loadBookingDetail(long id) {
        Call<BookingResponse> call = bookingApi.getBookingById(id);
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    BookingResponse booking = response.body();
                    if (booking != null) {
                        tvBookingId.setText("Mã đặt vé: #" + booking.getId());
                        tvMovieName.setText("Phim: " + booking.getMovieTitle());
                        tvCinema.setText("Rạp: " + booking.getCinemaName());
                        tvRoom.setText("Phòng: " + booking.getRoomName());
                        tvStartTime.setText("Giờ chiếu: " + booking.getStartTime());
                        tvSeats.setText("Ghế: " + booking.getSeatNumbers());
                        tvFood.setText("Đồ ăn & nước: " + booking.getFoodItemsText());
                        tvTotal.setText("Tổng tiền: " + booking.getTotalAmount() + " VNĐ");
                    }
                } else {
                    Log.e("BOOKING_ERROR", "Code: " + response.code() + ", Body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e("BOOKING_ERROR", "Error: " + t.getMessage());
            }
        });
    }
}