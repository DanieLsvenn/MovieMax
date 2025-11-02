package com.example.moviemax.Model.BookingDto;

import java.util.List;

public class BookingRequest {

    private long accountId;


    private long showtimeId;

    private List<Long> seatIds;

    public BookingRequest() {}

    public BookingRequest(long accountId, long showtimeId, List<Long> seatIds) {
        this.accountId = accountId;
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

}
