package com.example.moviemax.Api;

import android.content.Context;

import com.example.moviemax.Utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://103.200.20.174:8081/api/";

    // Add context parameter
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // Logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // SessionManager for token
            SessionManager sessionManager = new SessionManager(context);

            // Add interceptors with timeout configuration
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(sessionManager))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)    // Connection timeout
                    .readTimeout(60, TimeUnit.SECONDS)       // Read timeout  
                    .writeTimeout(30, TimeUnit.SECONDS)      // Write timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }
}
