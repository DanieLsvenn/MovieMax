package com.example.moviemax.Utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static JSONObject decode(String jwtEncoded) {
        try {
            String[] split = jwtEncoded.split("\\.");
            String body = getJson(split[1]);
            Log.d("JWT_DECODED", "Body: " + body);
            return new JSONObject(body);
        } catch (Exception e) {
            Log.e("JWT_DECODE_ERROR", "Error decoding token: " + e.getMessage());
            return null;
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
