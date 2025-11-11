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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.Dashboard.CinemaAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.CinemaApi;
import com.example.moviemax.Model.CinemaDto.CinemaRequest;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaFragment extends Fragment {

    private RecyclerView recyclerView;
    private CinemaAdapter adapter;
    private List<CinemaResponse> cinemaList = new ArrayList<CinemaResponse>();
    private LinearLayout listContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnCreate, btnSave;
    private EditText etName, etAddress, etPhone;

    private boolean listVisible = true;

    private String mode = "";
    private CinemaResponse selectedCinema;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cinema, container, false);

        // Views
        listContainer = view.findViewById(R.id.listContainer);
        recyclerView = view.findViewById(R.id.recyclerViewCinemas);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnDetails = view.findViewById(R.id.btnDetails);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etPhone = view.findViewById(R.id.etPhone);

        // Attach LayoutManager first
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        reloadList();

        // Initialize adapter
        adapter = new CinemaAdapter(requireContext(), cinemaList, cinema -> {
            // Handle item click
            selectedCinema = cinema;

            // Highlight the selected cinema
            adapter.setSelectedCinema(cinema);
        });

        recyclerView.setAdapter(adapter);

        arrowBtn.setOnClickListener(v -> toggleList());
        btnDetails.setOnClickListener(v -> displayDetails());
        btnEdit.setOnClickListener(v -> onEditBtnClick());
        btnSave.setOnClickListener(v -> onConfirm());
        btnDelete.setOnClickListener(v -> onDelete());
        btnCreate.setOnClickListener(v -> onCreateBtnClick());

        return view;
    }

    private void reloadList() {
        // Call Api
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);

        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (!response.isSuccessful()) {
                    try {
                        Log.e("API_RESPONSE_ERROR", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), "Unauthorized or empty data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                t.printStackTrace();
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "Failed to load data: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        listContainer.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    private void disableList() {
        listVisible = false;
        listContainer.setVisibility(View.GONE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_up);
    }

    private void enableList() {
        listVisible = true;
        listContainer.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void displayDetails() {
        if (selectedCinema == null) return;

        etName.setText(selectedCinema.getName());
        etAddress.setText(selectedCinema.getAddress());
        etPhone.setText(selectedCinema.getPhone());

        // Disable editing by default
        enableEditing(false);

        // Toggle the list
        disableList();

        // Scroll to the details view
        etName.post(() -> {
            assert getView() != null;
            ((ScrollView) getView()).smoothScrollTo(0, etName.getTop());
        });
    }

    private void enableEditing(boolean enabled) {
        etName.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        btnSave.setEnabled(enabled);
    }

    private void saveChanges() {
        String inputName = etName.getText().toString();
        String inputAddress = etAddress.getText().toString();
        String inputPhone = etPhone.getText().toString();

        if (mode == "Edit") {
            CinemaRequest cinemaRequest = new CinemaRequest(inputName, inputAddress, inputPhone);

            CinemaApi api = new ApiService().getClient(requireActivity()).create(CinemaApi.class);
            api.updateCinema(selectedCinema.getId(), cinemaRequest).enqueue(new Callback<CinemaResponse>() {
                @Override
                public void onResponse(Call<CinemaResponse> call, Response<CinemaResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CinemaResponse> call, Throwable t) {
                    t.printStackTrace();
                    Log.e("API_ERROR", t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to save changes: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            enableEditing(false);
            reloadList();
            enableList();
        } else if (mode == "Create") {
            CinemaRequest cinemaRequest = new CinemaRequest(inputName, inputAddress, inputPhone);
            CinemaApi api = new ApiService().getClient(requireActivity()).create(CinemaApi.class);
            api.createCinema(cinemaRequest).enqueue(new Callback<CinemaResponse>() {
                @Override
                public void onResponse(Call<CinemaResponse> call, Response<CinemaResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CinemaResponse> call, Throwable t) {
                    t.printStackTrace();
                    Log.e("API_ERROR", t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to save changes: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            enableEditing(false);
            reloadList();
            enableList();
        }
    }

    private void onConfirm() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) {
                // user pressed Yes
                saveChanges();
                reloadList();
                Toast.makeText(requireContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
            } else {
                // user pressed No
                Toast.makeText(requireContext(), "Changes cancelled!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCreateBtnClick() {
        enableEditing(true);
        mode = "Create";
    }

    private void onEditBtnClick() {
        enableEditing(true);
        mode = "Edit";
    }

    private void deleteCinema() {
        CinemaApi api = new ApiService().getClient(requireActivity()).create(CinemaApi.class);
        api.deleteCinema(selectedCinema.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireContext(), "Failed to save changes: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        enableEditing(false);
        reloadList();
        enableList();
    }

    private void onDelete() {
        showConfirmDialog("Delete this cinema?", confirmed -> {
            if (confirmed) {
                // user pressed Yes
                deleteCinema();
                reloadList();
                Toast.makeText(requireContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
            } else {
                // user pressed No
                Toast.makeText(requireContext(), "Changes cancelled!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showConfirmDialog(String message, ConfirmListener listener) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    listener.onConfirm(true);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    listener.onConfirm(false);
                })
                .show();
    }

    // Define an interface for callback
    public interface ConfirmListener {
        void onConfirm(boolean confirmed);
    }
}
