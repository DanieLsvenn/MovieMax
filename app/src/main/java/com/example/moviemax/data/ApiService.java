package com.example.moviemax.data;

import com.example.moviemax.Model.AccountDto.AccountResponse;
import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.Model.LoginDto.LoginRequest;
import com.example.moviemax.Model.LoginDto.LoginResponse;
import com.example.moviemax.Model.RegisterRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<LoginResponse> register(@Body RegisterRequest request);
    
    // Account endpoints
    @GET("/api/accounts/{id}")
    Call<AccountResponse> getAccount(@Path("id") long id);
    
    @PUT("/api/accounts/{id}")
    Call<AccountResponse> updateAccount(@Path("id") long id, @Body AccountResponse account);
    
    @DELETE("/api/accounts/{id}")
    Call<Void> deleteAccount(@Path("id") long id);
    
    // Booking endpoints
    @GET("/api/accounts/{accountsId}/bookings")
    Call<List<BookingResponse>> getUserBookings(@Path("accountsId") long accountsId);
}
