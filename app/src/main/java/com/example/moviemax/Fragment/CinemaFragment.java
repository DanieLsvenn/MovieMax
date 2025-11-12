package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.Dashboard.CinemaAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.CinemaApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.CinemaDto.CinemaRequest;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaFragment extends Fragment {
    private static final String TAG = "CinemaFragment";

    // UI Components
    private RecyclerView recyclerViewCinemas;
    private CinemaAdapter adapter;
    private CardView detailsContainer;
    private ImageButton arrowBtn;
    private Button btnAdd, btnDelete, btnSave;
    private EditText etName, etAddress, etPhone;
    private TextView tvTotalCinemas;

    // Data
    private List<CinemaResponse> cinemaList = new ArrayList<>();
    private CinemaResponse selectedCinema;
    private String mode = "";
    private boolean listVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cinema, container, false);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        reloadCinemaList();
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
        recyclerViewCinemas = view.findViewById(R.id.recyclerViewCinemas);

        // Containers and buttons
        detailsContainer = view.findViewById(R.id.detailsContainer);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        // Detail fields
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etPhone = view.findViewById(R.id.etPhone);

        // Stats
        tvTotalCinemas = view.findViewById(R.id.tvTotalCinemas);
    }

    private void setupRecyclerView() {
        recyclerViewCinemas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CinemaAdapter(requireContext(), cinemaList, cinema -> {
            selectedCinema = cinema;
            adapter.setSelectedCinema(cinema);
            displayCinemaDetails();
        });
        recyclerViewCinemas.setAdapter(adapter);
    }

    private void setupClickListeners() {
        arrowBtn.setOnClickListener(v -> toggleList());
        btnAdd.setOnClickListener(v -> onCreateCinema());
        btnDelete.setOnClickListener(v -> onDeleteCinema());
        btnSave.setOnClickListener(v -> onSaveCinema());
    }

    // ============ Cinema List Operations ============

    private void reloadCinemaList() {
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateStats();
                } else {
                    showToast("Failed to load cinemas");
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading cinemas", t);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void updateStats() {
        tvTotalCinemas.setText(String.valueOf(cinemaList.size()));
    }

    private void toggleList() {
        listVisible = !listVisible;
        recyclerViewCinemas.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    // ============ Cinema Details Display ============

    private void displayCinemaDetails() {
        if (selectedCinema == null) return;

        mode = "Edit";
        etName.setText(selectedCinema.getName());
        etAddress.setText(selectedCinema.getAddress());
        etPhone.setText(selectedCinema.getPhone());

        enableDetails();
        hideList();
    }

    // ============ Cinema CRUD Operations ============

    private void onCreateCinema() {
        mode = "Create";
        clearFields();
        enableDetails();
        hideList();
    }

    private void onSaveCinema() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) saveCinemaChanges();
        });
    }

    private void saveCinemaChanges() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        CinemaRequest cinemaReq = new CinemaRequest(name, address, phone);
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);

        if ("Edit".equals(mode) && selectedCinema != null) {
            api.updateCinema(selectedCinema.getId(), cinemaReq).enqueue(new Callback<CinemaResponse>() {
                @Override
                public void onResponse(Call<CinemaResponse> call, Response<CinemaResponse> response) {
                    showToast(response.isSuccessful() ? "Cinema updated!" : "Update failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<CinemaResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        } else if ("Create".equals(mode)) {
            api.createCinema(cinemaReq).enqueue(new Callback<CinemaResponse>() {
                @Override
                public void onResponse(Call<CinemaResponse> call, Response<CinemaResponse> response) {
                    showToast(response.isSuccessful() ? "Cinema created!" : "Create failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<CinemaResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        }
    }

    private void onDeleteCinema() {
        if (selectedCinema == null) return;

        showConfirmDialog("Delete this cinema?", confirmed -> {
            if (confirmed) deleteCinema();
        });
    }

    private void deleteCinema() {
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        api.deleteCinema(selectedCinema.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showToast(response.isSuccessful() ? "Cinema deleted!" : "Delete failed");
                if (response.isSuccessful()) resetAfterSave();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void resetAfterSave() {
        reloadCinemaList();
        showList();
        disableDetails();
    }

    // ============ UI Helper Methods ============

    private void clearFields() {
        etName.setText("");
        etAddress.setText("");
        etPhone.setText("");
    }

    private void enableDetails() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void disableDetails() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void showList() {
        listVisible = true;
        recyclerViewCinemas.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void hideList() {
        listVisible = false;
        recyclerViewCinemas.setVisibility(View.GONE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_up);
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
}