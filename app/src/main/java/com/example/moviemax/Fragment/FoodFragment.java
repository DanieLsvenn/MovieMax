package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviemax.Adapter.Dashboard.FoodAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.FoodApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.FoodDto.FoodItemRequest;
import com.example.moviemax.Model.FoodDto.FoodItemResponse;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodFragment extends Fragment {
    private static final String TAG = "FoodFragment";

    // UI Components
    private RecyclerView recyclerViewFoods;
    private FoodAdapter adapter;
    private CardView detailsContainer;
    private ImageButton arrowBtn;
    private Button btnEdit, btnDelete, btnAdd, btnSave;
    private EditText etName, etPrice, etDescription;

    // Image upload components
    private ImageView ivImagePreview;
    private Button btnSelectImage;
    private TextView tvImageUrl;
    private ProgressBar imageUploadProgress;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Data
    private List<FoodItemResponse> foodList = new ArrayList<>();
    private FoodItemResponse selectedFood;
    private String mode = "";
    private boolean listVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);
        initViews(view);
        setupImagePicker();
        setupRecyclerView();
        setupClickListeners();
        reloadFoodList();
        disableDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardInsetsHelper.applyKeyboardInsets(view);
    }

    private void initViews(View view) {
        // RecyclerView
        recyclerViewFoods = view.findViewById(R.id.recyclerViewFoods);

        // Containers and buttons
        detailsContainer = view.findViewById(R.id.detailsContainer);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        // Food detail fields
        etName = view.findViewById(R.id.etName);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription);

        // Image upload views
        ivImagePreview = view.findViewById(R.id.ivImagePreview);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        tvImageUrl = view.findViewById(R.id.tvImageUrl);
        imageUploadProgress = view.findViewById(R.id.imageUploadProgress);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK
                            && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                }
        );
    }

    private void setupRecyclerView() {
        recyclerViewFoods.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodAdapter(requireContext(), foodList, food -> {
            selectedFood = food;
            adapter.setSelectedFood(food);
            displayFoodDetails();
        });
        recyclerViewFoods.setAdapter(adapter);
    }

    private void setupClickListeners() {
        arrowBtn.setOnClickListener(v -> toggleList());
        btnAdd.setOnClickListener(v -> onCreateFood());
        btnEdit.setOnClickListener(v -> enableEditing());
        btnDelete.setOnClickListener(v -> onDeleteFood());
        btnSave.setOnClickListener(v -> onSaveFood());
        btnSelectImage.setOnClickListener(v -> openImagePicker());
    }

    // ============ Food List Operations ============

    private void reloadFoodList() {
        FoodApi api = ApiService.getClient(requireActivity()).create(FoodApi.class);
        api.getFoods().enqueue(new Callback<List<FoodItemResponse>>() {
            @Override
            public void onResponse(Call<List<FoodItemResponse>> call, Response<List<FoodItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodList.clear();
                    foodList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Failed to load foods");
                }
            }

            @Override
            public void onFailure(Call<List<FoodItemResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading foods", t);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        recyclerViewFoods.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    // ============ Food Details Display ============

    private void displayFoodDetails() {
        if (selectedFood == null) return;

        mode = "Edit";
        etName.setText(selectedFood.getName());
        etPrice.setText(String.valueOf(selectedFood.getPrice()));
        etDescription.setText(selectedFood.getDescription());

        loadExistingImage(selectedFood.getImageUrl());

        // Instantly enable editing
        enableDetails();
        hideList();
    }

    private void loadExistingImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = getFullImageUrl(imageUrl);

            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivImagePreview);

            tvImageUrl.setText(imageUrl);
            tvImageUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            ivImagePreview.setImageResource(R.drawable.ic_launcher_background);
            tvImageUrl.setText("No image set");
            tvImageUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private String getFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }
        return imageUrl.startsWith("http") ? imageUrl : SupabaseStorageHelper.getSupabaseImageUrl(imageUrl);
    }

    // ============ Image Upload Operations ============

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivImagePreview);

            tvImageUrl.setText("New image selected - will upload on save");
            tvImageUrl.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void uploadImage(OnUploadCompleteListener listener) {
        if (selectedImageUri == null) {
            listener.onComplete(null);
            return;
        }

        imageUploadProgress.setVisibility(View.VISIBLE);
        tvImageUrl.setText("Uploading image...");
        tvImageUrl.setTextColor(getResources().getColor(android.R.color.black));

        SupabaseStorageHelper.uploadImage(requireContext(), selectedImageUri,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String fileName) {
                        requireActivity().runOnUiThread(() -> {
                            imageUploadProgress.setVisibility(View.GONE);
                            tvImageUrl.setText(fileName);
                            tvImageUrl.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            listener.onComplete(fileName);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            imageUploadProgress.setVisibility(View.GONE);
                            tvImageUrl.setText("Upload failed: " + error);
                            tvImageUrl.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            showToast("Image upload failed: " + error);
                            listener.onComplete(null);
                        });
                    }
                });
    }

    // ============ Food CRUD Operations ============

    private void onCreateFood() {
        mode = "Create";
        clearFields();
        enableDetails();
        hideList();
    }

    private void onSaveFood() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) saveFoodChanges();
        });
    }

    private void saveFoodChanges() {
        if (selectedImageUri != null) {
            uploadImage(this::proceedWithSave);
        } else {
            String existingImageUrl = (selectedFood != null) ? selectedFood.getImageUrl() : null;
            proceedWithSave(existingImageUrl);
        }
    }

    private void proceedWithSave(String imageFileName) {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showToast("Please fill in all required fields");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("Invalid price format");
            return;
        }

        FoodItemRequest foodReq = new FoodItemRequest();
        foodReq.setPrice(price);
        foodReq.setName(name);
        foodReq.setDescription(description);

        if (imageFileName != null) {
            foodReq.setImageUrl(imageFileName);
        }

        FoodApi api = ApiService.getClient(requireActivity()).create(FoodApi.class);

        if ("Edit".equals(mode) && selectedFood != null) {
            foodReq.setFoodId(selectedFood.getId());
            api.updateFood(selectedFood.getId(), foodReq).enqueue(new Callback<FoodItemResponse>() {
                @Override
                public void onResponse(Call<FoodItemResponse> call, Response<FoodItemResponse> response) {
                    showToast(response.isSuccessful() ? "Food item updated!" : "Update failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<FoodItemResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        } else if ("Create".equals(mode)) {
            api.createFood(foodReq).enqueue(new Callback<FoodItemResponse>() {
                @Override
                public void onResponse(Call<FoodItemResponse> call, Response<FoodItemResponse> response) {
                    showToast(response.isSuccessful() ? "Food item created!" : "Create failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<FoodItemResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        }
    }

    private void onDeleteFood() {
        if (selectedFood == null) return;

        showConfirmDialog("Delete this food item?", confirmed -> {
            if (confirmed) deleteFood();
        });
    }

    private void deleteFood() {
        FoodApi api = ApiService.getClient(requireActivity()).create(FoodApi.class);
        api.deleteFood(selectedFood.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showToast(response.isSuccessful() ? "Food item deleted!" : "Delete failed");
                if (response.isSuccessful()) resetAfterSave();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void resetAfterSave() {
        reloadFoodList();
        showList();
        disableDetails();
        selectedImageUri = null;
    }

    // ============ UI Helper Methods ============

    private void clearFields() {
        etName.setText("");
        etPrice.setText("");
        etDescription.setText("");

        selectedImageUri = null;
        ivImagePreview.setImageResource(R.drawable.ic_launcher_background);
        tvImageUrl.setText("No image selected");
        tvImageUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void enableDetails() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void disableDetails() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void showList() {
        listVisible = true;
        recyclerViewFoods.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void hideList() {
        listVisible = false;
        recyclerViewFoods.setVisibility(View.GONE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_up);
    }

    private void enableEditing() {
        // Fields are now always editable when details are shown
        showToast("Editing enabled");
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showConfirmDialog(String message, ConfirmListener listener) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> listener.onConfirm(true))
                .setNegativeButton("No", (d, w) -> listener.onConfirm(false))
                .show();
    }

    // ============ Interfaces ============

    public interface ConfirmListener {
        void onConfirm(boolean confirmed);
    }

    public interface OnUploadCompleteListener {
        void onComplete(String fileName);
    }
}