package com.example.moviemax.Api;

import com.example.moviemax.Model.MovieDto.MovieResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApi {
    @GET("movies")
    Call<List<MovieResponse>> getMovies();
    
    @GET("movies/{id}")
    Call<MovieResponse> getMovieById(@Path("id") int movieId);
    
    @GET("movies")
    Call<List<MovieResponse>> searchMovies(@Query("title") String title);
}
