package com.example.moviemax.Model.FoodDto;

import java.io.Serializable;

public class FoodItemRequest implements Serializable {
    private int foodId;
    private int quantity;

    public FoodItemRequest(){}

    public FoodItemRequest(int foodId, int quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

