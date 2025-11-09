package com.example.moviemax.data;

import com.example.moviemax.model.LoginRequest;
import com.example.moviemax.model.LoginResponse;
import com.example.moviemax.model.RegisterRequest;
import com.example.moviemax.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<LoginResponse> register(@Body RegisterRequest request);
}
