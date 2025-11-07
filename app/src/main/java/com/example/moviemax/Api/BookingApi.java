package com.example.moviemax.Api;

import com.example.moviemax.Model.BookingDto.BookingRequest;
import com.example.moviemax.Model.BookingDto.BookingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookingApi {
    @POST("/api/bookings")
    Call<BookingResponse> createBooking(@Body BookingRequest request);

    @GET("/api/bookings/{id}")
    Call<BookingResponse> getBookingById(@Path("id") long id);

    @PUT("/api/bookings/{id}/status")
    Call<Void> updateBookingStatus(@Path("id") long bookingId, @Query("status") String status);
}
