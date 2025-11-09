package com.example.moviemax.Model.LoginDto;

public class LoginResponse {
    private int id;
    private String fullName;
    private String email;
    private String gender;
    private String passWord;
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

    public String getPassWord() {
        return passWord;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }
}
