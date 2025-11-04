package com.example.moviemax.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "MovieMaxPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🔹 Lưu token
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    // 🔹 Lưu tên người dùng
    public void saveUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    // 🔹 Lưu email
    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // 🔹 Lấy token
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    // 🔹 Lấy tên
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    // 🔹 Lấy email
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // 🔹 Xóa dữ liệu khi logout
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
