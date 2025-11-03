package com.example.moviemax.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

public class ImageUploadTestActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ImageView ivPreview;
    private Button btnSelectImage, btnUpload, btnGoToMovies;
    private TextView tvStatus, tvImageUrl;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload_test);

        initViews();
        setupImagePicker();
        setupClickListeners();
        checkPermissions();
    }

    private void initViews() {
        ivPreview = findViewById(R.id.ivPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnGoToMovies = findViewById(R.id.btnGoToMovies);
        tvStatus = findViewById(R.id.tvStatus);
        tvImageUrl = findViewById(R.id.tvImageUrl);
        progressBar = findViewById(R.id.progressBar);

        btnUpload.setEnabled(false);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        btnUpload.setOnClickListener(v -> uploadImageToSupabase());

        btnGoToMovies.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivPreview);

            btnUpload.setEnabled(true);
            tvStatus.setText("Image selected. Ready to upload.");
        }
    }

    private void uploadImageToSupabase() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        btnUpload.setEnabled(false);
        tvStatus.setText("Uploading to Supabase...");

        SupabaseStorageHelper.uploadImage(this, selectedImageUri, new SupabaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnUpload.setEnabled(true);
                    uploadedImageUrl = imageUrl;

                    String fullUrl = SupabaseStorageHelper.getSupabaseImageUrl(imageUrl);
                    tvStatus.setText("✅ Upload successful!");
                    tvImageUrl.setText("Image URL: " + fullUrl);

                    Toast.makeText(ImageUploadTestActivity.this,
                            "Image uploaded successfully!", Toast.LENGTH_LONG).show();

                    Log.d("ImageUpload", "Uploaded URL: " + fullUrl);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnUpload.setEnabled(true);
                    tvStatus.setText("❌ Upload failed: " + error);

                    Toast.makeText(ImageUploadTestActivity.this,
                            "Upload failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}