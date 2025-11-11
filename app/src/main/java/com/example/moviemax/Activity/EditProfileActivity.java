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
    private AutoCompleteTextView etGender;
    private MaterialButton btnChangePassword;
    
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
        
        btnChangePassword = findViewById(R.id.btnChangePassword);
        
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
        
        btnChangePassword.setOnClickListener(v -> {
            // TODO: Navigate to change password activity
            Toast.makeText(this, "Password change will be implemented soon", Toast.LENGTH_SHORT).show();
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
        
        showLoading(true);
        hideError();
        
        // Create updated account object
        AccountResponse updatedAccount = new AccountResponse();
        updatedAccount.setId(currentAccount.getId());
        updatedAccount.setFullName(etFullName.getText().toString().trim());
        updatedAccount.setEmail(etEmail.getText().toString().trim());
        updatedAccount.setPhone(etPhone.getText().toString().trim());
        updatedAccount.setGender(etGender.getText().toString().trim());
        updatedAccount.setDateOfBirth(selectedDate.getTime());
        updatedAccount.setUsername(currentAccount.getUsername()); // Keep original username
        updatedAccount.setPassword(currentAccount.getPassword()); // Keep original password
        updatedAccount.setRole(currentAccount.getRole()); // Keep original role
        updatedAccount.setAuthorities(currentAccount.getAuthorities()); // Keep original authorities
        updatedAccount.setEnabled(currentAccount.isEnabled());
        updatedAccount.setAccountNonExpired(currentAccount.isAccountNonExpired());
        updatedAccount.setAccountNonLocked(currentAccount.isAccountNonLocked());
        updatedAccount.setCredentialsNonExpired(currentAccount.isCredentialsNonExpired());
        updatedAccount.setCreatedAt(currentAccount.getCreatedAt());
        updatedAccount.setUpdatedAt(new Date()); // Set current timestamp
        
        Call<AccountResponse> call = apiService.updateAccount(currentAccount.getId(), updatedAccount);
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
                    showError("Failed to update profile: " + response.code());
                    Log.e(TAG, "Error updating profile: " + response.code() + " - " + response.message());
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
        String email = etEmail.getText().toString().trim();
        
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
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