package com.example.moviemax.Model.BookingDto;

import com.google.gson.annotations.SerializedName;

/**
 * Class đại diện cho food item trong BookingResponse
 * (KHÁC với FoodItemRequest - cái này dùng để nhận response từ API)
 */
public class FoodItemResponse {

    @SerializedName("foodName")
    private String foodName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    @SerializedName("subtotal")
    private double subtotal;

    // Constructor
    public FoodItemResponse() {}

    // Getters and Setters
    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return foodName + " x" + quantity + " = " + subtotal + "đ";
    }
}
