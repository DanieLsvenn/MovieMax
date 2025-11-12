package com.example.moviemax.Model.UpdateProfileDto;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@JsonAdapter(UpdateProfileRequest.UpdateProfileSerializer.class)
public class UpdateProfileRequest {
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("dateOfBirth")
    private Date dateOfBirth;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("oldPassword")
    private String oldPassword;
    
    @SerializedName("newPassword")
    private String newPassword;
    
    // Custom serializer that excludes empty password fields
    public static class UpdateProfileSerializer implements JsonSerializer<UpdateProfileRequest> {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        static {
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        
        @Override
        public JsonElement serialize(UpdateProfileRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            // Always include basic profile fields
            if (src.fullName != null) {
                jsonObject.addProperty("fullName", src.fullName);
            }
            if (src.gender != null) {
                jsonObject.addProperty("gender", src.gender);
            }
            if (src.dateOfBirth != null) {
                jsonObject.addProperty("dateOfBirth", DATE_FORMAT.format(src.dateOfBirth));
            }
            if (src.phone != null) {
                jsonObject.addProperty("phone", src.phone);
            }
            
            // Only include password fields if both are provided and not empty
            if (src.oldPassword != null && !src.oldPassword.trim().isEmpty() &&
                src.newPassword != null && !src.newPassword.trim().isEmpty()) {
                jsonObject.addProperty("oldPassword", src.oldPassword);
                jsonObject.addProperty("newPassword", src.newPassword);
            }
            
            return jsonObject;
        }
    }

    // Constructors
    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName, String gender, Date dateOfBirth, 
                              String phone, String oldPassword, String newPassword) {
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}