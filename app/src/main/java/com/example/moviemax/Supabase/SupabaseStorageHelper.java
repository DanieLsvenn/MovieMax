package com.example.moviemax.Supabase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.moviemax.Supabase.SupabaseConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageHelper {
    private static final String TAG = "SupabaseStorage";
    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String fileName);
        void onError(String error);
    }

    /**
     * Upload an image to Supabase Storage
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @param callback Callback for success/error
     */
    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Read image file from URI
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    callback.onError("Cannot read image file");
                    return;
                }

                // Create temporary file
                File tempFile = File.createTempFile("upload", ".jpg", context.getCacheDir());
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                // Copy input stream to file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                // Generate unique filename
                String fileName = "poster_" + UUID.randomUUID().toString() + ".jpg";

                Log.d(TAG, "Starting upload for file: " + fileName);

                // Upload to Supabase
                uploadToSupabase(tempFile, fileName, new UploadCallback() {
                    @Override
                    public void onSuccess(String uploadedFileName) {
                        tempFile.delete();
                        Log.d(TAG, "Upload successful, file deleted");
                        callback.onSuccess(uploadedFileName);
                    }

                    @Override
                    public void onError(String error) {
                        tempFile.delete();
                        Log.e(TAG, "Upload failed: " + error);
                        callback.onError(error);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Upload preparation error: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Upload file to Supabase Storage using REST API
     */
    private static void uploadToSupabase(File file, String fileName, UploadCallback callback) {
        try {
            // Create request body from file
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));

            // Build upload URL
            String uploadUrl = SupabaseConfig.getStorageUploadUrl() + "/" + fileName;

            Log.d(TAG, "Upload URL: " + uploadUrl);

            // Build request with Supabase auth header
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                    .addHeader("Content-Type", "image/jpeg")
                    .post(fileBody)
                    .build();

            // Execute async request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage(), e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = "";
                    try {
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }

                        if (response.isSuccessful()) {
                            Log.d(TAG, "Upload successful! Response: " + responseBody);
                            callback.onSuccess(fileName);
                        } else {
                            Log.e(TAG, "Upload failed - Code: " + response.code() + ", Body: " + responseBody);
                            callback.onError("Upload failed (Code " + response.code() + "): " + responseBody);
                        }
                    } finally {
                        response.close();
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Upload error: " + e.getMessage(), e);
            callback.onError("Upload error: " + e.getMessage());
        }
    }

    /**
     * Get the full public URL for an uploaded image
     * @param fileName The filename returned from upload
     * @return Full public URL
     */
    public static String getSupabaseImageUrl(String fileName) {
        return SupabaseConfig.getPublicUrl(fileName);
    }
}