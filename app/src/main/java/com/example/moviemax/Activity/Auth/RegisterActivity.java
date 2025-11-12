package com.example.moviemax.Activity.Auth;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.AuthApi;
import com.example.moviemax.Model.RegisterDto.RegisterRequest;
import com.example.moviemax.Model.LoginDto.LoginResponse;
import com.example.moviemax.Model.RegisterDto.RegisterResponse;
import com.example.moviemax.R;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etFullName, etPassword, etPhone, etDateOfBirth;
    private RadioButton rbMale, rbFemale;
    private AuthApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ view
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        Button btnRegister = findViewById(R.id.btnRegister);

        apiService = ApiService.getClient(this).create(AuthApi.class);

        // 🔹 Gắn DatePickerDialog thay vì cho nhập tay
        etDateOfBirth.setFocusable(false);
        etDateOfBirth.setClickable(true);
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: YYYY-MM-DD
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etDateOfBirth.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String date = etDateOfBirth.getText().toString().trim();
        String gender = rbMale.isChecked() ? "m" : "fm";

        if (email.isEmpty() || fullName.isEmpty() || password.isEmpty() || phone.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Format ISO chuẩn backend
        String isoDate = date + "T00:00:00.000Z";
        RegisterRequest req = new RegisterRequest(email, fullName, password, phone, gender, isoDate);

        apiService.register(req).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "🎉 Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay về LoginActivity
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có phản hồi";
                        Toast.makeText(RegisterActivity.this, "❌ Đăng ký thất bại!\n" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "⚠️ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
