package com.example.moviemax.Api;


import com.example.moviemax.Model.CinemaDto.CinemaRequest;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.Model.RoomDto.RoomResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CinemaApi {
    @GET("cinemas")
    Call<List<CinemaResponse>> getCinemas();

    @GET("cinemas/{cinemaId}/rooms")
    Call<List<RoomResponse>> getRoomsByCinema(@Path("cinemaId") Integer cinemaId);

    @PUT("cinemas/{id}")
    Call<CinemaResponse> updateCinema(@Path("id") Integer id, @Body CinemaRequest cinema);

    @DELETE("cinemas/{id}")
    Call<Void> deleteCinema(@Path("id") Integer id);

    @POST("cinemas")
    Call<CinemaResponse> createCinema(@Body CinemaRequest cinema);
}
