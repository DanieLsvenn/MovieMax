package com.example.moviemax.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moviemax.Model.AccountDto.AccountResponse;
import com.example.moviemax.R;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.AuthApi;
import com.example.moviemax.Utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoFragment extends Fragment {
    private TextView tvUserName, tvUserEmail, tvFullName, tvGender, tvPhone, 
                     tvDateOfBirth, tvUsername, tvRole, tvMemberSince, tvError;
    private ProgressBar progressBar;
    
    private AuthApi apiService;
    private SessionManager sessionManager;
    
    private static final String TAG = "UserInfoFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        
        initViews(view);
        initServices();
        loadUserInfo();
        
        return view;
    }

    private void initViews(View view) {
        // Header info
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        
        // Personal information
        tvFullName = view.findViewById(R.id.tvFullName);
        tvGender = view.findViewById(R.id.tvGender);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth);
        
        // Account information
        tvUsername = view.findViewById(R.id.tvUsername);
        tvRole = view.findViewById(R.id.tvRole);
        tvMemberSince = view.findViewById(R.id.tvMemberSince);
        
        // UI elements
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
    }

    private void initServices() {
        apiService = ApiService.getClient(requireActivity()).create(AuthApi.class);
        sessionManager = new SessionManager(requireContext());
    }

    private void loadUserInfo() {
        showLoading(true);
        hideError();
        
        int accountId = sessionManager.getAccountId();
        if (accountId == -1) {
            showError("No account information found. Please login again.");
            showLoading(false);
            return;
        }

        Call<AccountResponse> call = apiService.getAccount(accountId);
        call.enqueue(new Callback<AccountResponse>() {
            @Override
            public void onResponse(Call<AccountResponse> call, Response<AccountResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayUserInfo(response.body());
                } else {
                    showError("Failed to load user information: " + response.code());
                    Log.e(TAG, "Error loading user info: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AccountResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading user info", t);
            }
        });
    }

    private void displayUserInfo(AccountResponse account) {
        try {
            // Header information
            tvUserName.setText(account.getFullName() != null ? account.getFullName() : "N/A");
            tvUserEmail.setText(account.getEmail() != null ? account.getEmail() : "N/A");
            
            // Personal information
            tvFullName.setText(account.getFullName() != null ? account.getFullName() : "N/A");
            tvGender.setText(account.getGender() != null ? account.getGender() : "N/A");
            tvPhone.setText(account.getPhone() != null ? account.getPhone() : "N/A");
            
            // Format and display date of birth
            if (account.getDateOfBirth() != null) {
                tvDateOfBirth.setText(formatDate(account.getDateOfBirth()));
            } else {
                tvDateOfBirth.setText("N/A");
            }
            
            // Account information
            tvUsername.setText(account.getUsername() != null ? account.getUsername() : "N/A");
            tvRole.setText(account.getRole() != null ? account.getRole() : "USER");
            
            // Format and display member since
            if (account.getCreatedAt() != null) {
                tvMemberSince.setText(formatDate(account.getCreatedAt()));
            } else {
                tvMemberSince.setText("N/A");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying user info", e);
            showError("Error displaying user information");
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return formatter.format(date);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
        
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void hideError() {
        if (tvError != null) {
            tvError.setVisibility(View.GONE);
        }
    }

    public void refreshUserInfo() {
        loadUserInfo();
    }
}