package com.example.moviemax.Api;

import com.example.moviemax.Model.FoodDto.FoodItemRequest;
import com.example.moviemax.Model.FoodDto.FoodItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FoodApi {
    // Get all foods
    @GET("/api/food")
    Call<List<FoodItemResponse>> getFoods();

    // Get food by ID
    @GET("/api/food/{id}")
    Call<FoodItemResponse> getFoodById(@Path("id") int id);

    // Create new food
    @POST("/api/food")
    Call<FoodItemResponse> createFood(@Body FoodItemRequest foodRequest);

    // Update existing food
    @PUT("/api/food/{id}")
    Call<FoodItemResponse> updateFood(@Path("id") int id, @Body FoodItemRequest foodRequest);

    // Delete food
    @DELETE("/api/food/{id}")
    Call<Void> deleteFood(@Path("id") int id);
}
