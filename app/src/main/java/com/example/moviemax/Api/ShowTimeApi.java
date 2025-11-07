package com.example.moviemax.Api;

import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ShowTimeApi {
    @GET("/api/showtimes")
    Call<List<ShowTimeResponse>> getShowTimes();

    // API lấy showtimes theo movieId
    @GET("/api/showtimes")
    Call<List<ShowTimeResponse>> getShowTimesByMovie(@Query("movieId") long movieId);

    // API lấy showtime theo ID
    @GET("/api/showtimes/{id}")
    Call<ShowTimeResponse> getShowTimeById(@Path("id") long id);
}