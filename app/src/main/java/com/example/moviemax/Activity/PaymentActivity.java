package com.example.moviemax.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.BookingApi;
import com.example.moviemax.Api.SeatApi;
import com.example.moviemax.Helper.PaymentHelper;
import com.example.moviemax.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PaymentActivity handles payment processing for movie ticket bookings
 * Supports MoMo, ZaloPay, and Cash payment methods
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // UI Components
    private ImageButton btnBack;
    private TextView tvMovieTitle, tvCinemaInfo, tvSeatInfo, tvTotalAmount;
    private CardView cvMomo, cvZaloPay, cvCash;
    private Button btnConfirmPayment;

    // Booking Data
    private long bookingId;
    private double totalAmount;
    private String movieTitle, cinemaName, roomName, startTime, seatNumbers;
    private int seatCount;
    private ArrayList<Integer> selectedSeatIds;

    // Payment
    private String selectedPaymentMethod = "";
    private BookingApi bookingApi;
    private SeatApi seatApi;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        initApis();
        getIntentData();
        displayBookingInfo();
        setupListeners();

        executorService = Executors.newSingleThreadExecutor();

        // Check if opened via deep link (payment callback)
        handleDeepLink(getIntent());
    }

    /**
     * Initialize UI components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCinemaInfo = findViewById(R.id.tvCinemaInfo);
        tvSeatInfo = findViewById(R.id.tvSeatCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        cvMomo = findViewById(R.id.cvMomo);
        cvZaloPay = findViewById(R.id.cvZaloPay);
        cvCash = findViewById(R.id.cvCash);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    /**
     * Initialize API services
     */
    private void initApis() {
        bookingApi = ApiService.getClient().create(BookingApi.class);
        seatApi = ApiService.getClient().create(SeatApi.class);
    }

    /**
     * Get booking data from intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        bookingId = intent.getLongExtra("BOOKING_ID", -1);
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0);
        movieTitle = intent.getStringExtra("MOVIE_TITLE");
        cinemaName = intent.getStringExtra("CINEMA_NAME");
        roomName = intent.getStringExtra("ROOM_NAME");
        startTime = intent.getStringExtra("START_TIME");
        seatCount = intent.getIntExtra("SEAT_COUNT", 0);
        seatNumbers = intent.getStringExtra("SEAT_NUMBERS");
        selectedSeatIds = intent.getIntegerArrayListExtra("SELECTED_SEAT_IDS");

        Log.d(TAG, "=== PAYMENT ACTIVITY DATA ===");
        Log.d(TAG, "BookingId: " + bookingId);
        Log.d(TAG, "TotalAmount: " + totalAmount);
        Log.d(TAG, "SeatIds: " + (selectedSeatIds != null ? selectedSeatIds.toString() : "null"));
    }

    /**
     * Display booking information
     */
    private void displayBookingInfo() {
        tvMovieTitle.setText(movieTitle != null ? movieTitle : "Movie Title");

        String formattedTime = formatTime(startTime);
        tvCinemaInfo.setText(cinemaName + " " + roomName + " • " + formattedTime);

        String seatInfo = seatCount + " ghế";
        if (seatNumbers != null && !seatNumbers.isEmpty()) {
            seatInfo += " (" + seatNumbers + ")";
        }
        tvSeatInfo.setText(seatInfo);

        tvTotalAmount.setText(formatPrice((int) totalAmount));
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        cvMomo.setOnClickListener(v -> selectPaymentMethod("MOMO"));
        cvZaloPay.setOnClickListener(v -> selectPaymentMethod("ZALOPAY"));
        cvCash.setOnClickListener(v -> selectPaymentMethod("CASH"));

        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    /**
     * Select payment method and update UI
     */
    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        resetCardColors();

        int selectedColor = getResources().getColor(R.color.selected_payment, null);
        switch (method) {
            case "MOMO":
                cvMomo.setCardBackgroundColor(selectedColor);
                break;
            case "ZALOPAY":
                cvZaloPay.setCardBackgroundColor(selectedColor);
                break;
            case "CASH":
                cvCash.setCardBackgroundColor(selectedColor);
                break;
        }

        btnConfirmPayment.setEnabled(true);
        Log.d(TAG, "✅ Selected payment method: " + method);
    }

    /**
     * Reset all payment card colors
     */
    private void resetCardColors() {
        int defaultColor = getResources().getColor(R.color.card_background, null);
        cvMomo.setCardBackgroundColor(defaultColor);
        cvZaloPay.setCardBackgroundColor(defaultColor);
        cvCash.setCardBackgroundColor(defaultColor);
    }

    /**
     * Process payment based on selected method
     */
    private void processPayment() {
        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookingId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đặt vé", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmPayment.setEnabled(false);
        Log.d(TAG, "=== PROCESSING PAYMENT ===");
        Log.d(TAG, "Method: " + selectedPaymentMethod);
        Log.d(TAG, "BookingId: " + bookingId);
        Log.d(TAG, "Amount: " + totalAmount);

        String orderInfo = "Thanh toán vé " + movieTitle;

        switch (selectedPaymentMethod) {
            case "MOMO":
                processMoMoPayment(orderInfo);
                break;
            case "ZALOPAY":
                processZaloPayPayment(orderInfo);
                break;
            case "CASH":
                processCashPayment();
                break;
        }
    }

    /**
     * Process MoMo Payment
     * Creates payment request and opens MoMo payment page in browser
     */
    private void processMoMoPayment(String orderInfo) {
        Log.d(TAG, "🔄 Creating MoMo payment for booking: " + bookingId);
        Toast.makeText(this, "Đang tạo thanh toán MoMo...", Toast.LENGTH_SHORT).show();

        executorService.execute(() -> {
            String payUrl = PaymentHelper.createMoMoPayment(bookingId, totalAmount, orderInfo);

            runOnUiThread(() -> {
                if (payUrl != null && !payUrl.isEmpty()) {
                    Log.d(TAG, "✅ MoMo payment URL created");
                    openPaymentUrl(payUrl);
                } else {
                    Log.e(TAG, "❌ Failed to create MoMo payment");
                    Toast.makeText(this, "Không thể tạo thanh toán MoMo", Toast.LENGTH_SHORT).show();
                    btnConfirmPayment.setEnabled(true);
                }
            });
        });
    }

    /**
     * Process ZaloPay Payment
     * Creates payment request and opens ZaloPay payment page in browser
     */
    private void processZaloPayPayment(String description) {
        Log.d(TAG, "🔄 Creating ZaloPay payment for booking: " + bookingId);
        Toast.makeText(this, "Đang tạo thanh toán ZaloPay...", Toast.LENGTH_SHORT).show();

        executorService.execute(() -> {
            String payUrl = PaymentHelper.createZaloPayPayment(bookingId, totalAmount, description);

            runOnUiThread(() -> {
                if (payUrl != null && !payUrl.isEmpty()) {
                    Log.d(TAG, "✅ ZaloPay payment URL created");
                    openPaymentUrl(payUrl);
                } else {
                    Log.e(TAG, "❌ Failed to create ZaloPay payment");
                    Toast.makeText(this, "Không thể tạo thanh toán ZaloPay", Toast.LENGTH_SHORT).show();
                    btnConfirmPayment.setEnabled(true);
                }
            });
        });
    }

    /**
     * Process Cash Payment
     * For cash payment, immediately update booking status
     */
    private void processCashPayment() {
        Log.d(TAG, "🔄 Processing Cash payment for booking: " + bookingId);

        updateBookingAndSeatStatuses(true, () -> {
            Toast.makeText(PaymentActivity.this,
                    "Đặt vé thành công! Vui lòng thanh toán tại quầy",
                    Toast.LENGTH_LONG).show();
            goToBookingSuccess();
        });
    }

    /**
     * Open payment URL in browser
     */
    private void openPaymentUrl(String url) {
        try {
            Log.d(TAG, "🌐 Opening payment URL in browser");

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(browserIntent);

            Toast.makeText(this, "Đang chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Cannot open payment URL: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
            btnConfirmPayment.setEnabled(true);
        }
    }

    /**
     * Handle new intent when activity is resumed via deep link
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "🔄 New intent received (deep link callback)");
        handleDeepLink(intent);
    }

    /**
     * Handle deep link from payment gateway
     */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data == null) {
            Log.d(TAG, "No deep link data found");
            return;
        }

        Log.d(TAG, "🔗 Deep link received: " + data.toString());
        String path = data.getPath();

        if (path != null) {
            if (path.contains("momo")) {
                handleMoMoCallback(data);
            } else if (path.contains("zalopay")) {
                handleZaloPayCallback(data);
            }
        }
    }

    /**
     * Handle MoMo payment callback
     * Format: moviemax://payment/momo?resultCode=0&message=Success&orderId=xxx
     */
    private void handleMoMoCallback(Uri data) {
        String resultCode = data.getQueryParameter("resultCode");
        String message = data.getQueryParameter("message");
        String orderId = data.getQueryParameter("orderId");

        Log.d(TAG, "=== MOMO CALLBACK ===");
        Log.d(TAG, "ResultCode: " + resultCode);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "OrderId: " + orderId);

        // MoMo: resultCode = 0 means success
        boolean isSuccess = "0".equals(resultCode);
        handlePaymentResult(isSuccess, "MoMo", message);
    }

    /**
     * Handle ZaloPay payment callback
     * Format: moviemax://payment/zalopay?status=1&apptransid=xxx
     */
    private void handleZaloPayCallback(Uri data) {
        String status = data.getQueryParameter("status");
        String appTransId = data.getQueryParameter("apptransid");

        Log.d(TAG, "=== ZALOPAY CALLBACK ===");
        Log.d(TAG, "Status: " + status);
        Log.d(TAG, "AppTransId: " + appTransId);

        // ZaloPay: status = 1 means success
        boolean isSuccess = "1".equals(status);
        handlePaymentResult(isSuccess, "ZaloPay", null);
    }

    /**
     * Handle payment result after receiving callback
     */
    private void handlePaymentResult(boolean isSuccess, String paymentMethod, String message) {
        if (isSuccess) {
            Log.d(TAG, "✅ " + paymentMethod + " payment successful!");

            updateBookingAndSeatStatuses(true, () -> {
                Toast.makeText(this,
                        "Thanh toán " + paymentMethod + " thành công!",
                        Toast.LENGTH_SHORT).show();
                goToBookingSuccess();
            });

        } else {
            Log.w(TAG, "❌ " + paymentMethod + " payment failed!");

            updateBookingAndSeatStatuses(false, null);

            runOnUiThread(() -> {
                String errorMsg = message != null ? message : "Thanh toán thất bại";
                Toast.makeText(this,
                        paymentMethod + ": " + errorMsg + ". Ghế đã được giải phóng.",
                        Toast.LENGTH_LONG).show();

                btnConfirmPayment.setEnabled(true);
                resetCardColors();
                selectedPaymentMethod = "";
            });
        }
    }

    /**
     * Update booking and seat statuses after payment
     */
    private void updateBookingAndSeatStatuses(boolean isSuccess, Runnable onComplete) {
        Log.d(TAG, "=== UPDATING DATABASE ===");
        Log.d(TAG, "Payment Success: " + isSuccess);

        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            Log.e(TAG, "❌ ERROR: No seat IDs to update!");
            if (onComplete != null) onComplete.run();
            return;
        }

        // 1. Update booking status
        String bookingStatus = isSuccess ? "SUCCESS" : "FAILED";
        bookingApi.updateBookingStatus(bookingId, bookingStatus).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Booking #" + bookingId + " updated to " + bookingStatus);
                } else {
                    Log.e(TAG, "❌ Failed to update booking: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "❌ Error updating booking: " + t.getMessage());
            }
        });

        // 2. Update seat status
        String seatStatus = isSuccess ? "BOOKED" : "AVAILABLE";
        Log.d(TAG, "📍 Updating " + selectedSeatIds.size() + " seats to " + seatStatus);

        for (Integer seatId : selectedSeatIds) {
            seatApi.updateSeatStatus(seatId, seatStatus).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Seat #" + seatId + " updated to " + seatStatus);
                    } else {
                        Log.e(TAG, "❌ Failed to update seat #" + seatId + ": " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "❌ Error updating seat #" + seatId + ": " + t.getMessage());
                }
            });
        }

        if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Navigate to booking success screen
     */
    private void goToBookingSuccess() {
        Intent intent = new Intent(PaymentActivity.this, BookingSuccessActivity.class);
        intent.putExtra("BOOKING_ID", bookingId);
        intent.putExtra("MOVIE_TITLE", movieTitle);
        intent.putExtra("CINEMA_NAME", cinemaName);
        intent.putExtra("ROOM_NAME", roomName);
        intent.putExtra("START_TIME", startTime);
        intent.putExtra("SEAT_COUNT", seatCount);
        intent.putExtra("SEAT_NUMBERS", seatNumbers);
        intent.putExtra("TOTAL_AMOUNT", totalAmount);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "🗑️ Activity destroyed");
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Format time from ISO format to readable format
     */
    private String formatTime(String startTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(startTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + e.getMessage());
            return startTime != null ? startTime : "";
        }
    }

    /**
     * Format price with thousand separator
     */
    private String formatPrice(int price) {
        return String.format(Locale.getDefault(), "%,d đ", price);
    }
}