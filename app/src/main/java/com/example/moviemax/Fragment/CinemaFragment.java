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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.CinemaAdapter;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CinemaFragment extends Fragment {

    private RecyclerView recyclerView;
    private CinemaAdapter adapter;
    private List<CinemaResponse> cinemaList;
    private LinearLayout listContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnSave;
    private EditText etName, etAddress, etPhone;

    private boolean listVisible = true;
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
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etPhone = view.findViewById(R.id.etPhone);

        // Attach LayoutManager first
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sample data
        cinemaList = new ArrayList<>();
        cinemaList.add(new CinemaResponse() {{
            setId(1);
            setName("CGV Vincom Landmark 81");
            setAddress("772 Điện Biên Phủ, Bình Thạnh, HCM");
            setPhone("02812345678");
        }});
        cinemaList.add(new CinemaResponse() {{
            setId(2);
            setName("Galaxy Nguyễn Du");
            setAddress("116 Nguyễn Du, Q1, HCM");
            setPhone("02887654321");
        }});

        // Initialize adapter
        adapter = new CinemaAdapter(requireContext(), cinemaList, cinema -> {
            // Handle item click
            selectedCinema = cinema;

            // Highlight the selected cinema
            adapter.setSelectedCinema(cinema);

//            // Collapse the list
//            listVisible = false;
//            listContainer.setVisibility(View.GONE);
//            arrowBtn.setImageResource(R.drawable.ic_arrow_up);
//
//            // Show details below
//            displayDetails();
        });

        recyclerView.setAdapter(adapter);

        arrowBtn.setOnClickListener(v -> toggleList());
        btnDetails.setOnClickListener(v -> displayDetails());
        btnEdit.setOnClickListener(v -> enableEditing(true));
        btnSave.setOnClickListener(v -> showConfirmDialog("Save changes?"));
        btnDelete.setOnClickListener(v -> showConfirmDialog("Delete this cinema?"));

        return view;
    }

    private void addCinema(String name, String address, String phone) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("address", address);
        item.put("phone", phone);
    }

    private void toggleList() {
        listVisible = !listVisible;
        listContainer.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    private void disableList(){
        listVisible = false;
        listContainer.setVisibility(View.GONE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_up);
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

    private void showConfirmDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) ->
                        Toast.makeText(requireContext(), "Confirmed", Toast.LENGTH_SHORT).show())
                .setNegativeButton("No", null)
                .show();
    }
}
