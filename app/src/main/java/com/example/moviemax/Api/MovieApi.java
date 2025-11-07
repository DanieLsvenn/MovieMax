package com.example.moviemax.Api;

import com.example.moviemax.Model.MovieDTO.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieApi {
    @GET("/api/movies/{id}")
    Call<MovieResponse> getMovieById(@Path("id") long id);
}
