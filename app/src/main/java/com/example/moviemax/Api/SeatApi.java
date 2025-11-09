package com.example.moviemax.Api;

import com.example.moviemax.Model.SeatDto.SeatResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SeatApi {
    @GET("/api/showtimes/{showtimeId}/seats")
    Call<List<SeatResponse>> getSeatsByShowtime(@Path("showtimeId") long showtimeId);

    @PUT("/api/seats/{id}/status")
    Call<Void> updateSeatStatus(@Path("id") int seatId, @Query("status") String status);
}
