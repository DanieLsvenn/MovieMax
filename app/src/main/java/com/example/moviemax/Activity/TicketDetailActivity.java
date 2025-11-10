package com.example.moviemax.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.BookingApi;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.Model.BookingDto.BookingFoodItem;
import com.example.moviemax.Model.Cinema;
import com.example.moviemax.R;
import com.example.moviemax.Utils.CinemaDataProvider;
import com.example.moviemax.Utils.DistanceUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private TextView tvBookingId, tvMovieName, tvSeats, tvTotal, tvCinema, tvRoom, tvStartTime, tvFood, tvBookingDate, tvStatus;
    private TextView tvCinemaAddress, tvDistance;
    private Button btnGetDirections;
    private BookingApi bookingApi;
    
    // Map and location related
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private Cinema currentCinema;
    private Location userLocation;

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
        
        // Map related views
        tvCinemaAddress = findViewById(R.id.tvCinemaAddress);
        tvDistance = findViewById(R.id.tvDistance);
        btnGetDirections = findViewById(R.id.btnGetDirections);

        bookingApi = ApiService.getClient().create(BookingApi.class);
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize map fragment
        initializeMap();
        
        // Set up directions button click listener
        btnGetDirections.setOnClickListener(v -> openDirections());

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
        
        // Show cinema location on map
        showCinemaLocation(booking.getCinemaName());
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
    
    private void initializeMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        // Request location permission and get user location
        requestLocationPermission();
    }
    
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để hiển thị khoảng cách", Toast.LENGTH_SHORT).show();
                // Still show the cinema location even without user location
                if (currentCinema != null) {
                    showCinemaOnMap(currentCinema);
                }
            }
        }
    }
    
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = location;
                            updateDistanceDisplay();
                        }
                        
                        // Show cinema location regardless of user location
                        if (currentCinema != null) {
                            showCinemaOnMap(currentCinema);
                        }
                    }
                });
        }
    }
    
    private void showCinemaLocation(String cinemaName) {
        currentCinema = CinemaDataProvider.getCinemaByName(cinemaName);
        
        if (currentCinema != null) {
            // Display cinema address
            tvCinemaAddress.setText(currentCinema.getAddress());
            
            // Update distance if we have user location
            updateDistanceDisplay();
            
            // Show cinema on map if map is ready
            if (mMap != null) {
                showCinemaOnMap(currentCinema);
            }
            
            // Show directions button
            btnGetDirections.setVisibility(Button.VISIBLE);
        } else {
            tvCinemaAddress.setText("Địa chỉ rạp: Không tìm thấy thông tin");
            tvDistance.setText("");
            btnGetDirections.setVisibility(Button.GONE);
        }
    }
    
    private void showCinemaOnMap(Cinema cinema) {
        if (mMap == null) return;
        
        LatLng cinemaLocation = new LatLng(cinema.getLatitude(), cinema.getLongitude());
        
        // Clear existing markers
        mMap.clear();
        
        // Add marker for cinema
        mMap.addMarker(new MarkerOptions()
            .position(cinemaLocation)
            .title(cinema.getName())
            .snippet(cinema.getAddress()));
        
        // Move camera to cinema location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cinemaLocation, 15));
    }
    
    private void updateDistanceDisplay() {
        if (currentCinema != null && userLocation != null) {
            float distance = DistanceUtil.calculateDistanceInMeters(
                userLocation, 
                currentCinema.getLatitude(), 
                currentCinema.getLongitude()
            );
            
            String distanceText = "Khoảng cách: " + DistanceUtil.formatDistance(distance);
            tvDistance.setText(distanceText);
        } else {
            tvDistance.setText("Khoảng cách: Không xác định được vị trí");
        }
    }
    
    private void openDirections() {
        if (currentCinema == null) {
            Toast.makeText(this, "Không tìm thấy thông tin rạp", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create Google Maps intent for directions
        String uri = String.format(Locale.ENGLISH, 
            "http://maps.google.com/maps?daddr=%f,%f (%s)", 
            currentCinema.getLatitude(), 
            currentCinema.getLongitude(),
            currentCinema.getName());
        
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser if Google Maps app is not installed
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        }
    }
}