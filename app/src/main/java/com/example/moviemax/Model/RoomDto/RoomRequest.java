package com.example.moviemax.Model.RoomDto;

public class RoomRequest {
    private String name;

    private int totalSeats;

    private String roomType;

    private long cinemaId;

    public RoomRequest() {
    }


    public RoomRequest(String name, int totalSeats, String roomType, long cinemaId) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.roomType = roomType;
        this.cinemaId = cinemaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public long getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(long cinemaId) {
        this.cinemaId = cinemaId;
    }
}
