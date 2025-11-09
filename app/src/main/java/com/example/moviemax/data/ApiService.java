package com.example.moviemax.data;

import com.example.moviemax.Model.LoginDto.LoginRequest;
import com.example.moviemax.Model.LoginDto.LoginResponse;
import com.example.moviemax.Model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<LoginResponse> register(@Body RegisterRequest request);
}
