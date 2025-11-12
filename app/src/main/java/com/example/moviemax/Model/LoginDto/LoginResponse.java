package com.example.moviemax.Model.LoginDto;
public class LoginResponse {
    private int id;
    private String fullName;
    private String email;
    private String gender;
    private String password;
    private String phone;
    private String token;
    public String role;

    public int getId() { return id; }
    public String getFullName() {
        return fullName;
    }
    public String getEmail() {
        return email;
    }
    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }
    public String getRole() { return role; }
}

