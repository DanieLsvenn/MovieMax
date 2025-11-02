package com.example.moviemax.Api;

import com.example.moviemax.Model.MovieDto.MovieResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MovieApi {
    @GET("movies")
    Call<List<MovieResponse>> getMovies();
}
