package com.example.moviemax.Api;

import com.example.moviemax.Model.RoomDto.RoomRequest;
import com.example.moviemax.Model.RoomDto.RoomResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RoomApi {
    @GET("rooms")
    Call<List<RoomResponse>> getRooms();

    @GET("rooms/{id}")
    Call<RoomResponse> getRoomById(@Path("id") int id);

    @POST("rooms")
    Call<RoomResponse> createRoom(@Body RoomRequest roomRequest);

    @PUT("rooms/{id}")
    Call<RoomResponse> updateRoom(@Path("id") int id, @Body RoomRequest roomRequest);

    @DELETE("rooms/{id}")
    Call<Void> deleteRoom(@Path("id") int id);
}
