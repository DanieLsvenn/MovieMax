package com.example.moviemax.Model.SeatDto;

public class SeatRequest {

    private String status;

    public SeatRequest(){}

    public SeatRequest(String status){
        this.status = status;
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }
}
