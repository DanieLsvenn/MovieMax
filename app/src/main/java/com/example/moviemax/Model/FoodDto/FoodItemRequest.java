package com.example.moviemax.Model.FoodDto;

import java.io.Serializable;

public class FoodItemRequest implements Serializable {
    private int foodId;
    private String name;
    private double price;
    private String description;
    private String imageUrl;


    public FoodItemRequest(){}

    public FoodItemRequest(int foodId, String name, double price, String description, String imageUrl) {
        this.foodId = foodId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

