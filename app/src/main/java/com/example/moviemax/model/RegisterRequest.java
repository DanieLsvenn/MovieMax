package com.example.moviemax.Model;

public class RegisterRequest {
    private String email;
    private String fullName;
    private String password;
    private String phone;
    private String gender;
    private String dateOfBirth;

    public RegisterRequest(String email, String fullName, String password, String phone, String gender, String dateOfBirth) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    // getter & setter
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
}
