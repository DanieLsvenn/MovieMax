package com.example.moviemax.Model.RegisterDto;

public class RegisterResponse {
    private int id;
    private String fullName;
    private String email;
    private String gender;
    private String phone;
    private String token;

    // Getters
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }
}
