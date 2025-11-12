package com.example.moviemax.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.Model.AccountDto.AccountResponse;
import com.example.moviemax.Model.UpdateProfileDto.UpdateProfileRequest;
import com.example.moviemax.R;
import com.example.moviemax.Utils.SessionManager;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.AuthApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    
    private static final String TAG = "EditProfileActivity";
    
    // UI Components
    private ImageButton btnBack, btnSave;
    private ProgressBar progressBar;
    private TextView tvError, tvChangePhoto;
    private CircleImageView ivProfileImage;
    private TextInputEditText etFullName, etEmail, etPhone, etDateOfBirth, etUsername;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private AutoCompleteTextView etGender;
    
    // Data
    private AccountResponse currentAccount;
    private AuthApi apiService;
    private SessionManager sessionManager;
    private SimpleDateFormat dateFormatter;
    
    // Date picker
    private Calendar selectedDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        initViews();
        initServices();
        setupGenderDropdown();
        setupClickListeners();
        loadCurrentUserData();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etUsername = findViewById(R.id.etUsername);
        etGender = findViewById(R.id.etGender);
        
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        selectedDate = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }
    
    private void initServices() {
        apiService = ApiService.getClient(this).create(AuthApi.class);
        sessionManager = new SessionManager(this);
    }
    
    private void setupGenderDropdown() {
        String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, genderOptions);
        etGender.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> saveProfile());
        
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        
        tvChangePhoto.setOnClickListener(v -> {
            // TODO: Implement photo selection
            Toast.makeText(this, "Photo selection will be implemented soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadCurrentUserData() {
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
                    currentAccount = response.body();
                    populateFields();
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
    
    private void populateFields() {
        if (currentAccount == null) return;
        
        etFullName.setText(currentAccount.getFullName());
        etEmail.setText(currentAccount.getEmail());
        etPhone.setText(currentAccount.getPhone());
        etUsername.setText(currentAccount.getUsername());
        etGender.setText(currentAccount.getGender(), false);
        
        if (currentAccount.getDateOfBirth() != null) {
            etDateOfBirth.setText(dateFormatter.format(currentAccount.getDateOfBirth()));
            selectedDate.setTime(currentAccount.getDateOfBirth());
        }
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, monthOfYear, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, monthOfYear);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDateOfBirth.setText(dateFormatter.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set maximum date to current date (user can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void saveProfile() {
        if (!validateInput()) {
            return;
        }
        
        // Check authentication before making the request
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            showError("Authentication token is missing. Please login again.");
            Log.e(TAG, "No auth token found");
            return;
        }
        
        // Verify we have valid account data
        if (currentAccount == null || currentAccount.getId() <= 0) {
            showError("Invalid account data. Please try reloading the page.");
            Log.e(TAG, "Invalid account data: " + (currentAccount != null ? currentAccount.getId() : "null"));
            return;
        }
        
        // Check if the logged-in user ID matches the account being updated
        int loggedInAccountId = sessionManager.getAccountId();
        if (loggedInAccountId != currentAccount.getId()) {
            showError("You can only update your own profile.");
            Log.e(TAG, "Account ID mismatch. Logged in: " + loggedInAccountId + ", Trying to update: " + currentAccount.getId());
            return;
        }
        
        showLoading(true);
        hideError();
        
        // Create update profile request with the new API format
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(etFullName.getText().toString().trim());
        request.setGender(etGender.getText().toString().trim());
        request.setDateOfBirth(selectedDate.getTime());
        request.setPhone(etPhone.getText().toString().trim());
        
        // Handle password change if provided
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        
        // Only set password fields if both are provided and not empty
        if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
            request.setOldPassword(oldPassword);
            request.setNewPassword(newPassword);
        }
        // Password fields will be excluded from JSON if not set (handled by custom serializer)
        
        // Debug: Log what we're about to send
        Log.d(TAG, "Sending update request:");
        Log.d(TAG, "- Full Name: " + request.getFullName());
        Log.d(TAG, "- Gender: " + request.getGender());  
        Log.d(TAG, "- Phone: " + request.getPhone());
        Log.d(TAG, "- Date of Birth: " + request.getDateOfBirth());
        boolean hasPasswordChange = (request.getOldPassword() != null && !request.getOldPassword().isEmpty() &&
                                   request.getNewPassword() != null && !request.getNewPassword().isEmpty());
        Log.d(TAG, "- Password change requested: " + hasPasswordChange);
        if (hasPasswordChange) {
            Log.d(TAG, "- Password fields will be included in request");
        } else {
            Log.d(TAG, "- Password fields will be excluded from request (no change requested)");
        }
        
        // Log the request details for debugging  
        Log.d(TAG, "Updating profile for account ID: " + currentAccount.getId());
        Log.d(TAG, "Auth token exists: " + (sessionManager.getAuthToken() != null));
        Log.d(TAG, "Request data - FullName: " + request.getFullName() + 
                   ", Gender: " + request.getGender() + 
                   ", Phone: " + request.getPhone() +
                   ", Has password change: " + (request.getOldPassword() != null));
        
        Call<AccountResponse> call = apiService.updateAccount(currentAccount.getId(), request);
        call.enqueue(new Callback<AccountResponse>() {
            @Override
            public void onResponse(Call<AccountResponse> call, Response<AccountResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Set result to indicate successful update
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    // Enhanced error logging
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    String errorMessage = "Failed to update profile (Error " + response.code() + ")";
                    if (response.code() == 403) {
                        errorMessage = "Access denied. Please check your login status.";
                        // Log more details for 403 errors
                        Log.e(TAG, "403 Forbidden error. Token: " + sessionManager.getAuthToken());
                        Log.e(TAG, "Account ID: " + sessionManager.getAccountId());
                    }
                    
                    showError(errorMessage);
                    Log.e(TAG, "Error updating profile: " + response.code() + " - " + response.message());
                    Log.e(TAG, "Error body: " + errorBody);
                }
            }
            
            @Override
            public void onFailure(Call<AccountResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error updating profile", t);
            }
        });
    }
    
    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        
        // Validate password fields (optional)
        if (!oldPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            // If any password field is filled, all must be filled
            if (oldPassword.isEmpty()) {
                etOldPassword.setError("Current password is required when changing password");
                etOldPassword.requestFocus();
                return false;
            }
            
            if (newPassword.isEmpty()) {
                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return false;
            }
            
            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Please confirm your new password");
                etConfirmPassword.requestFocus();
                return false;
            }
            
            if (newPassword.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
                etNewPassword.requestFocus();
                return false;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return false;
            }
            
            if (oldPassword.equals(newPassword)) {
                etNewPassword.setError("New password must be different from current password");
                etNewPassword.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
    
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}