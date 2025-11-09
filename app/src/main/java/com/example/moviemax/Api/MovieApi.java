package com.example.moviemax.Api;

import com.example.moviemax.Model.MovieDto.MovieRequest;
import com.example.moviemax.Model.MovieDto.MovieResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MovieApi {
    @GET("movies")
    Call<List<MovieResponse>> getMovies();

    @GET("movies/{id}")
    Call<MovieResponse> getMovieById(@Path("id") int id);

    @PUT("movies/{id}")
    Call<MovieResponse> updateMovie(@Path("id") int id, @Body MovieRequest movie);

    @DELETE("movies/{id}")
    Call<Void> deleteMovie(@Path("id") int id);

    @POST("movies")
    Call<MovieResponse> createMovie(@Body MovieRequest movie);
}
