package com.example.moviemax.Model.ShowTimeDto;

public class ShowTimeRequest {
    private String startTime;

    private  double price;

    private   long movieId;

    private long roomId;

    public ShowTimeRequest(){

    }


    public ShowTimeRequest(String startTime,double price, long movieId,long roomId){
        this.startTime = startTime;
        this.price = price;
        this.movieId = movieId;
        this.roomId = roomId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
