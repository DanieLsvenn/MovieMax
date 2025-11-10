package com.example.moviemax.Model.BookingDto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingResponse {
    @SerializedName("id")
    private long id;

    @SerializedName("movieTitle")
    private String movieTitle;

    @SerializedName("cinemaName")
    private String cinemaName;

    @SerializedName("roomName")
    private String roomName;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("seats")
    private List<String> seats;

    @SerializedName("foodItems")
    private List<BookingFoodItem> foodItems;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("bookingStatus")
    private String bookingStatus;

    @SerializedName("accountUsername")
    private String accountUsername;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public List<BookingFoodItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<BookingFoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }

    // Helper methods
    public String getSeatNumbers() {
        if (seats != null && !seats.isEmpty()) {
            return String.join(", ", seats);
        }
        return "";
    }

    public int getSeatCount() {
        return seats != null ? seats.size() : 0;
    }

    public String getFoodItemsText() {
        if (foodItems != null && !foodItems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < foodItems.size(); i++) {
                BookingFoodItem item = foodItems.get(i);
                sb.append(item.getFoodName())
                        .append(" x")
                        .append(item.getQuantity());

                if (i < foodItems.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return "Không có";
    }

    public int getFoodItemsCount() {
        return foodItems != null ? foodItems.size() : 0;
    }
}