package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CinemaFragment extends Fragment {

    private ListView cinemaListView;
    private LinearLayout listContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnSave;
    private EditText etName, etAddress, etPhone;

    private boolean listVisible = true;
    private ArrayList<Map<String, String>> cinemaData;
    private Map<String, String> selectedCinema;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cinema, container, false);

        // Views
        listContainer = view.findViewById(R.id.listContainer);
        cinemaListView = view.findViewById(R.id.cinemaListView);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnDetails = view.findViewById(R.id.btnDetails);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etPhone = view.findViewById(R.id.etPhone);

        loadDummyData();
        setupListView();

        btnDetails.setOnClickListener(v -> toggleList());
        btnEdit.setOnClickListener(v -> enableEditing(true));
        btnSave.setOnClickListener(v -> showConfirmDialog("Save changes?"));
        btnDelete.setOnClickListener(v -> showConfirmDialog("Delete this cinema?"));

        return view;
    }

    private void loadDummyData() {
        cinemaData = new ArrayList<>();
        addCinema("CGV Vincom Landmark 81", "772 Điện Biên Phủ, Bình Thạnh, HCM", "02812345678");
        addCinema("Galaxy Nguyễn Du", "116 Nguyễn Du, Q1, HCM", "02887654321");
    }

    private void addCinema(String name, String address, String phone) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("address", address);
        item.put("phone", phone);
        cinemaData.add(item);
    }

    private void setupListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                cinemaData.stream().map(item -> item.get("name")).toArray(String[]::new)
        );
        cinemaListView.setAdapter(adapter);

        cinemaListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinema = cinemaData.get(position);
            displayDetails();
            toggleList(); // Retract after choosing
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        listContainer.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    private void displayDetails() {
        if (selectedCinema == null) return;

        etName.setText(selectedCinema.get("name"));
        etAddress.setText(selectedCinema.get("address"));
        etPhone.setText(selectedCinema.get("phone"));

        // Disable editing by default
        enableEditing(false);

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

    private void showConfirmDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) ->
                        Toast.makeText(requireContext(), "Confirmed", Toast.LENGTH_SHORT).show())
                .setNegativeButton("No", null)
                .show();
    }
}
