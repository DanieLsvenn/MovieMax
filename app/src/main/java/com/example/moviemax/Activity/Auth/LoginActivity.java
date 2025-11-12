package com.example.moviemax.Activity.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.Activity.DashboardActivity;
import com.example.moviemax.Activity.HomeActivity;
import com.example.moviemax.Model.LoginDto.LoginRequest;
import com.example.moviemax.Model.LoginDto.LoginResponse;
import com.example.moviemax.R;
import com.example.moviemax.Utils.SessionManager;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private AuthApi apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initListeners();

        apiService = ApiService.getClient(this).create(AuthApi.class);
        sessionManager = new SessionManager(this);
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar); // optional, add a ProgressBar in layout
    }

    private void initListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void handleLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(phone, password)) return;

        showLoading(true);
        LoginRequest request = new LoginRequest(phone, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    showToast("Sai số điện thoại hoặc mật khẩu!");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    private boolean validateInput(String phone, String password) {
        if (phone.isEmpty() || password.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        if (phone.length() < 8 || phone.length() > 12) {
            showToast("Số điện thoại không hợp lệ!");
            return false;
        }
        if (password.length() < 4) {
            showToast("Mật khẩu quá ngắn!");
            return false;
        }
        return true;
    }

    private void handleLoginSuccess(LoginResponse response) {
        sessionManager.saveAuthToken(response.getToken());
        sessionManager.saveUserName(response.getFullName());
        sessionManager.saveUserEmail(response.getEmail());
        sessionManager.saveAccountId(response.getId());
        sessionManager.saveRole(response.getRole());

        if (response.getRole() != null && response.getRole().equals("ADMIN")) {
            showToast("Đăng nhập admin thành công!");
            navigateToDashboard(response);
        } else {
            showToast("Đăng nhập thành công!");
            navigateToHome(response);
        }
    }

    private void navigateToHome(LoginResponse response) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard(LoginResponse response) {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
