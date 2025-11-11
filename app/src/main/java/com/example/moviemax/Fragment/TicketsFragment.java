package com.example.moviemax.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.moviemax.Activity.HomeActivity;
import com.example.moviemax.Activity.TicketDetailActivity;
import com.example.moviemax.Adapter.BookingAdapter;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.R;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.AuthApi;
import com.example.moviemax.Utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {
    private RecyclerView recyclerViewTickets;
    private BookingAdapter bookingAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout errorLayout, emptyLayout;
    private FrameLayout ticketsLayout;
    private TextView tvError;
    private Button btnRefresh, btnRetry, btnBrowseMovies;
    
    private AuthApi apiService;
    private SessionManager sessionManager;
    private List<BookingResponse> bookingList;
    
    private static final String TAG = "TicketsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        
        initViews(view);
        initServices();
        setupRecyclerView();
        setupClickListeners();
        loadTickets();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewTickets = view.findViewById(R.id.recyclerViewTickets);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        errorLayout = view.findViewById(R.id.errorLayout);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        ticketsLayout = view.findViewById(R.id.ticketsLayout);
        tvError = view.findViewById(R.id.tvError);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnBrowseMovies = view.findViewById(R.id.btnBrowseMovies);
    }

    private void initServices() {
        apiService = ApiService.getClient(requireActivity()).create(AuthApi.class);
        sessionManager = new SessionManager(requireContext());
        bookingList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(requireContext(), bookingList);
        bookingAdapter.setOnBookingClickListener(this);
        
        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTickets.setAdapter(bookingAdapter);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> loadTickets());
        btnRetry.setOnClickListener(v -> loadTickets());
        
        btnBrowseMovies.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HomeActivity.class);
            startActivity(intent);
        });
        
        swipeRefreshLayout.setOnRefreshListener(this::loadTickets);
    }

    private void loadTickets() {
        showLoading(true);
        hideAllLayouts();
        
        int accountId = sessionManager.getAccountId();
        if (accountId == -1) {
            showError("No account information found. Please login again.");
            showLoading(false);
            return;
        }

        Call<List<BookingResponse>> call = apiService.getUserBookings(accountId);
        call.enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    handleTicketsLoaded(response.body());
                } else {
                    showError("Failed to load tickets: " + response.code());
                    Log.e(TAG, "Error loading tickets: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading tickets", t);
            }
        });
    }

    private void handleTicketsLoaded(List<BookingResponse> tickets) {
        bookingList.clear();
        if (tickets != null && !tickets.isEmpty()) {
            Log.d(TAG, "Total bookings received: " + tickets.size());
            
            // Filter to show only successfully completed bookings
            for (BookingResponse booking : tickets) {
                String status = booking.getBookingStatus();
                Log.d(TAG, "Booking #" + booking.getId() + " status: '" + status + "' (Movie: " + booking.getMovieTitle() + ")");
                
                // Include bookings with successful payment statuses
                if (status != null) {
                    String statusLower = status.toLowerCase();
                    // Check for various successful booking status values
                    if (statusLower.equals("success") || 
                        statusLower.equals("confirmed") || 
                        statusLower.equals("active") || 
                        statusLower.equals("paid") ||
                        statusLower.equals("completed") ||
                        statusLower.equals("complete") ||
                        statusLower.equals("booked")) {
                        
                        bookingList.add(booking);
                        Log.d(TAG, "✅ Including booking with successful status: " + status);
                    } else if (statusLower.equals("pending") || 
                              statusLower.equals("cancelled") || 
                              statusLower.equals("failed") ||
                              statusLower.equals("canceled")) {
                        Log.d(TAG, "❌ Excluding booking with unsuccessful status: " + status);
                    } else {
                        // For unknown statuses, include them and log for investigation
                        bookingList.add(booking);
                        Log.w(TAG, "⚠️ Unknown status, including anyway: " + status);
                    }
                }
            }
            
            Log.d(TAG, "Successfully filtered bookings: " + bookingList.size() + "/" + tickets.size());
            
            if (!bookingList.isEmpty()) {
                bookingAdapter.updateBookings(bookingList);
                showTicketsList();
            } else {
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideAllLayouts() {
        errorLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        ticketsLayout.setVisibility(View.GONE);
    }

    private void showError(String message) {
        hideAllLayouts();
        tvError.setText(message);
        errorLayout.setVisibility(View.VISIBLE);
        
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showEmptyState() {
        hideAllLayouts();
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void showTicketsList() {
        hideAllLayouts();
        ticketsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBookingClick(BookingResponse booking) {
        Log.d(TAG, "User clicked on booking: " + booking.getId() + " (" + booking.getMovieTitle() + ")");
        
        // Navigate to ticket detail activity
        Intent intent = new Intent(requireContext(), TicketDetailActivity.class);
        intent.putExtra("BOOKING_ID", booking.getId());
        intent.putExtra("MOVIE_TITLE", booking.getMovieTitle());
        intent.putExtra("CINEMA_NAME", booking.getCinemaName());
        intent.putExtra("ROOM_NAME", booking.getRoomName());
        intent.putExtra("START_TIME", booking.getStartTime());
        intent.putExtra("BOOKING_DATE", booking.getBookingDate());
        intent.putExtra("TOTAL_AMOUNT", booking.getTotalAmount());
        intent.putExtra("BOOKING_STATUS", booking.getBookingStatus());
        
        // Convert seats list to array for intent
        if (booking.getSeats() != null) {
            String[] seatsArray = booking.getSeats().toArray(new String[0]);
            intent.putExtra("SEATS", seatsArray);
        }
        
        startActivity(intent);
    }

    public void refreshTickets() {
        loadTickets();
    }

    // Method to filter tickets by status
    public void filterTicketsByStatus(String status) {
        List<BookingResponse> filteredList = new ArrayList<>();
        
        for (BookingResponse booking : bookingList) {
            if (status == null || status.isEmpty() || 
                (booking.getBookingStatus() != null && 
                 booking.getBookingStatus().equalsIgnoreCase(status))) {
                filteredList.add(booking);
            }
        }
        
        bookingAdapter.updateBookings(filteredList);
        
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            showTicketsList();
        }
    }
}