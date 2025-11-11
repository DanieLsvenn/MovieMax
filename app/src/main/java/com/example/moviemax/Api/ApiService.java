package com.example.moviemax.Api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
public class ApiService {
    // Biến static để giữ instance duy nhất của Retrofit (Singleton Pattern)
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://103.200.20.174:8081/api/";

    public static Retrofit getClient() {
        if (retrofit == null) {

            // 1. Logging Interceptor (Giữ lại để debug API, rất hữu ích)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Cấu hình OkHttpClient (ĐÃ BỎ AuthInterceptor)
            OkHttpClient client = new OkHttpClient.Builder()
                    // Chỉ thêm Logging Interceptor
                    .addInterceptor(logging)
                    .build();

            // 3. Tạo Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client) // Sử dụng OkHttpClient không cần token
                    .build();
        }

        return retrofit;
    }
}

