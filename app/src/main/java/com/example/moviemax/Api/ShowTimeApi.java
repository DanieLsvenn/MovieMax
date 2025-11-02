package com.example.moviemax.Api;

import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ShowTimeApi {
    @GET("showtimes")
    Call<List<ShowTimeResponse>> getShowTimes();
}
