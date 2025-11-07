package com.example.moviemax.Api;

import com.example.moviemax.Model.FoodDto.FoodItemsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FoodApi {
    @GET("/api/food")
    Call<List<FoodItemsResponse>> getFoods();
}
