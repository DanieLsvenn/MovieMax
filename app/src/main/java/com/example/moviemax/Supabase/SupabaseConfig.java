package com.example.moviemax.Supabase;

public class SupabaseConfig {

    // Replace with your Supabase credentials
    // Example: "https://abcdefghijklmnop.supabase.co"
    private static final String SUPABASE_URL = "https://caoaeoaapefvaxwhwgsx.supabase.co";

    // Your Supabase anon/public key
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNhb2Flb2FhcGVmdmF4d2h3Z3N4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIwOTk5NTcsImV4cCI6MjA3NzY3NTk1N30.J2ngQAq9nJVDxKQxmyDaxbMqPVj13WD5aLsKZ9wiodg";

    // The name of your storage bucket
    private static final String BUCKET_NAME = "movie-image";

    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }

    public static String getSupabaseKey() {
        return SUPABASE_KEY;
    }

    public static String getBucketName() {
        return BUCKET_NAME;
    }

    /**
     * Get the upload URL for Supabase Storage
     * Format: https://YOUR_PROJECT.supabase.co/storage/v1/object/BUCKET_NAME
     */
    public static String getStorageUploadUrl() {
        return SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME;
    }

    /**
     * Get the public URL for an uploaded file
     * Format: https://YOUR_PROJECT.supabase.co/storage/v1/object/public/BUCKET_NAME/FILENAME
     */
    public static String getPublicUrl(String fileName) {
        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
    }
}