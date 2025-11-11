package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Adapter.Dashboard.CinemaAdapter;
import com.example.moviemax.Adapter.Dashboard.RoomAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.CinemaApi;
import com.example.moviemax.Api.RoomApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.Model.RoomDto.RoomRequest;
import com.example.moviemax.Model.RoomDto.RoomResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private CinemaAdapter cinemaAdapter;
    private List<RoomResponse> roomList = new ArrayList<>();
    private List<CinemaResponse> cinemaList = new ArrayList<>();

    private CardView listContainer, detailsContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnAdd, btnSave;

    private EditText etName, etSeats, etType;
    private boolean listVisible = true;
    private String mode = "";
    private RoomResponse selectedRoom;

    private Spinner spinnerCinema;

    private int selectedCinemaId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

//        listContainer = view.findViewById(R.id.listContainer);

//        detailsContainer = view.findViewById(R.id.detailsContainer);
        recyclerView = view.findViewById(R.id.recyclerViewRooms);
//        arrowBtn = view.findViewById(R.id.arrowBtn);
//        btnDetails = view.findViewById(R.id.btnDetails);
//        btnAdd = view.findViewById(R.id.btnAdd);
//        btnEdit = view.findViewById(R.id.btnEdit);
//        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        etName = view.findViewById(R.id.etName);
        etSeats = view.findViewById(R.id.etSeats);
        etType = view.findViewById(R.id.etType);
        spinnerCinema = view.findViewById(R.id.spinnerCinema);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RoomAdapter(requireContext(), roomList, room -> {
            selectedRoom = room;
            adapter.setSelectedRoom(room);
            displayDetails();
        });
        recyclerView.setAdapter(adapter);

        reloadList();

//        arrowBtn.setOnClickListener(v -> toggleList());
//        btnDetails.setOnClickListener(v -> toggleList());
//        btnEdit.setOnClickListener(v -> onEditBtnClick());
//        btnAdd.setOnClickListener(v -> onCreateBtnClick());
//        btnDelete.setOnClickListener(v -> onDelete());
//        btnSave.setOnClickListener(v -> onConfirm());

        disableDetails();
        setupSpinnerListener();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardInsetsHelper.applyKeyboardInsets(view);
    }

    private void reloadList() {
        RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);
        api.getRooms().enqueue(new Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, Response<List<RoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        listContainer.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    private void disableDetails() {
//        detailsContainer.setVisibility(View.GONE);
    }

    private void enableDetails() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void displayDetails() {
        if (selectedRoom == null) return;

        etName.setText(selectedRoom.getName());
        etSeats.setText(String.valueOf(selectedRoom.getTotalSeats()));
        etType.setText(selectedRoom.getRoomType());

        enableEditing(false);
        enableDetails();
        listContainer.setVisibility(View.GONE);

        // Fetch all cinemas and set spinner properly
        CinemaApi cinemaApi = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        cinemaApi.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());

                    ArrayAdapter<CinemaResponse> cinemaAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            cinemaList
                    );
                    cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCinema.setAdapter(cinemaAdapter);

                    // Preselect the correct cinema
                    for (int i = 0; i < cinemaList.size(); i++) {
                        if (cinemaList.get(i).getName().equals(selectedRoom.getCinemaName())) {
                            spinnerCinema.setSelection(i);
                            selectedCinemaId = cinemaList.get(i).getId();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed to load cinemas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinnerListener() {
        spinnerCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CinemaResponse selectedCinema = (CinemaResponse) parent.getItemAtPosition(position);
                selectedCinemaId = selectedCinema.getId();
                Log.d("RoomFragment", "Selected cinema: " + selectedCinema.getName() + " (ID: " + selectedCinemaId + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCinemaId = -1;
            }
        });
    }

    private void enableEditing(boolean enabled) {
        etName.setEnabled(enabled);
        etSeats.setEnabled(enabled);
        etType.setEnabled(enabled);
//        etCinemaId.setEnabled(enabled);
        spinnerCinema.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        enableDetails();
    }

    private void onCreateBtnClick() {
        enableEditing(true);
        mode = "Create";
        clearFields();
        listContainer.setVisibility(View.GONE);
        enableDetails();

        // Create adapter for spinner (before API call)
        ArrayAdapter<CinemaResponse> cinemaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                cinemaList
        );
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(cinemaAdapter);

        // Fetch all cinemas from API
        CinemaApi cinemaApi = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        cinemaApi.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    cinemaAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), "Failed to load cinemas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        setupSpinnerListener();
    }

    private void onEditBtnClick() {
        if (selectedRoom == null) return;
        enableEditing(true);
        mode = "Edit";
    }

    private void clearFields() {
        etName.setText("");
        etSeats.setText("");
        etType.setText("");
//        etCinemaId.setText("");
    }

    private void saveChanges() {
        // Always get the latest selected cinema from the spinner
        if (spinnerCinema.getSelectedItem() != null) {
            CinemaResponse selectedCinema = (CinemaResponse) spinnerCinema.getSelectedItem();
            selectedCinemaId = selectedCinema.getId();
        }

        // Validate fields
        String name = etName.getText().toString().trim();
        String seatsText = etSeats.getText().toString().trim();
        String type = etType.getText().toString().trim();

        if (name.isEmpty() || seatsText.isEmpty() || type.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCinemaId == -1) {
            Toast.makeText(requireContext(), "Please select a cinema", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(seatsText);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Seats must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build request
        RoomRequest roomReq = new RoomRequest();
        roomReq.setName(name);
        roomReq.setTotalSeats(totalSeats);
        roomReq.setRoomType(type);
        roomReq.setCinemaId((long) selectedCinemaId);

        Log.d("RoomFragment", "Saving with cinemaId = " + selectedCinemaId);

        RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);

        // Handle Edit vs Create
        if ("Edit".equals(mode) && selectedRoom != null) {
            api.updateRoom(selectedRoom.getId(), roomReq).enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Room updated!" : "Update failed",
                            Toast.LENGTH_SHORT).show();
                    reloadList();
                }

                @Override
                public void onFailure(Call<RoomResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("Create".equals(mode)) {
            api.createRoom(roomReq).enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Room created!" : "Create failed",
                            Toast.LENGTH_SHORT).show();
                    reloadList();
                }

                @Override
                public void onFailure(Call<RoomResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Reset UI
        enableEditing(false);
        disableDetails();
        listContainer.setVisibility(View.VISIBLE);
    }

    private void onDelete() {
        if (selectedRoom == null) return;

        showConfirmDialog("Delete this room?", confirmed -> {
            if (confirmed) {
                RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);
                api.deleteRoom(selectedRoom.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(requireContext(),
                                response.isSuccessful() ? "Room deleted!" : "Delete failed",
                                Toast.LENGTH_SHORT).show();
                        reloadList();
                        disableDetails();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onConfirm() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) saveChanges();
        });
    }

    private void showConfirmDialog(String message, ConfirmListener listener) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> listener.onConfirm(true))
                .setNegativeButton("No", (d, w) -> listener.onConfirm(false))
                .show();
    }

    public interface ConfirmListener {
        void onConfirm(boolean confirmed);
    }
}
