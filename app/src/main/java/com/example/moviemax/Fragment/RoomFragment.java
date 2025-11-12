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
    private static final String TAG = "RoomFragment";

    // UI Components
    private RecyclerView recyclerViewRooms;
    private RoomAdapter adapter;
    private CardView detailsContainer;
    private ImageButton arrowBtn;
    private Button btnAdd, btnDelete, btnSave;
    private EditText etName, etSeats, etType;
    private AutoCompleteTextView spinnerCinema;
    private TextView tvTotalRooms, tvTotalSeats;

    // Data
    private List<RoomResponse> roomList = new ArrayList<>();
    private List<CinemaResponse> cinemaList = new ArrayList<>();
    private RoomResponse selectedRoom;
    private CinemaResponse selectedCinema;
    private String mode = "";
    private boolean listVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        reloadRoomList();
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
        recyclerViewRooms = view.findViewById(R.id.recyclerViewRooms);

        // Containers and buttons
        detailsContainer = view.findViewById(R.id.detailsContainer);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        // Detail fields
        etName = view.findViewById(R.id.etName);
        etSeats = view.findViewById(R.id.etSeats);
        etType = view.findViewById(R.id.etType);
        spinnerCinema = view.findViewById(R.id.spinnerCinema);

        // Stats
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvTotalSeats = view.findViewById(R.id.tvTotalSeats);
    }

    private void setupRecyclerView() {
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoomAdapter(requireContext(), roomList, room -> {
            selectedRoom = room;
            adapter.setSelectedRoom(room);
            displayRoomDetails();
        });
        recyclerViewRooms.setAdapter(adapter);
    }

    private void setupClickListeners() {
        arrowBtn.setOnClickListener(v -> toggleList());
        btnAdd.setOnClickListener(v -> onCreateRoom());
        btnDelete.setOnClickListener(v -> onDeleteRoom());
        btnSave.setOnClickListener(v -> onSaveRoom());
    }

    // ============ Room List Operations ============

    private void reloadRoomList() {
        RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);
        api.getRooms().enqueue(new Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, Response<List<RoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateStats();
                } else {
                    showToast("Failed to load rooms");
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading rooms", t);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void updateStats() {
        int totalRooms = roomList.size();
        int totalSeats = 0;
        for (RoomResponse room : roomList) {
            totalSeats += room.getTotalSeats();
        }
        tvTotalRooms.setText(String.valueOf(totalRooms));
        tvTotalSeats.setText(String.valueOf(totalSeats));
    }

    private void toggleList() {
        listVisible = !listVisible;
        recyclerViewRooms.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    // ============ Room Details Display ============

    private void displayRoomDetails() {
        if (selectedRoom == null) return;

        mode = "Edit";
        etName.setText(selectedRoom.getName());
        etSeats.setText(String.valueOf(selectedRoom.getTotalSeats()));
        etType.setText(selectedRoom.getRoomType());

        loadCinemasAndSelectCurrent();
        enableDetails();
        hideList();
    }

    private void loadCinemasAndSelectCurrent() {
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    setupCinemaSpinner();

                    // Preselect cinema if editing
                    if (selectedRoom != null) {
                        for (int i = 0; i < cinemaList.size(); i++) {
                            if (cinemaList.get(i).getName().equals(selectedRoom.getCinemaName())) {
                                spinnerCinema.setText(cinemaList.get(i).getName(), false);
                                selectedCinema = cinemaList.get(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading cinemas", t);
            }
        });
    }

    private void setupCinemaSpinner() {
        List<String> cinemaNames = new ArrayList<>();
        for (CinemaResponse cinema : cinemaList) {
            cinemaNames.add(cinema.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, cinemaNames);
        spinnerCinema.setAdapter(adapter);

        spinnerCinema.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinema = cinemaList.get(position);
        });

        spinnerCinema.setOnClickListener(v -> spinnerCinema.showDropDown());
    }

    // ============ Room CRUD Operations ============

    private void onCreateRoom() {
        mode = "Create";
        clearFields();
        loadCinemasAndSelectCurrent();
        enableDetails();
        hideList();
    }

    private void onSaveRoom() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) saveRoomChanges();
        });
    }

    private void saveRoomChanges() {
        String name = etName.getText().toString().trim();
        String seatsText = etSeats.getText().toString().trim();
        String type = etType.getText().toString().trim();

        if (name.isEmpty() || seatsText.isEmpty() || type.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (selectedCinema == null) {
            showToast("Please select a cinema");
            return;
        }

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(seatsText);
        } catch (NumberFormatException e) {
            showToast("Seats must be a number");
            return;
        }

        RoomRequest roomReq = new RoomRequest();
        roomReq.setName(name);
        roomReq.setTotalSeats(totalSeats);
        roomReq.setRoomType(type);
        roomReq.setCinemaId((long) selectedCinema.getId());

        RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);

        if ("Edit".equals(mode) && selectedRoom != null) {
            api.updateRoom(selectedRoom.getId(), roomReq).enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                    showToast(response.isSuccessful() ? "Room updated!" : "Update failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<RoomResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        } else if ("Create".equals(mode)) {
            api.createRoom(roomReq).enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                    showToast(response.isSuccessful() ? "Room created!" : "Create failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<RoomResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        }
    }

    private void onDeleteRoom() {
        if (selectedRoom == null) return;

        showConfirmDialog("Delete this room?", confirmed -> {
            if (confirmed) deleteRoom();
        });
    }

    private void deleteRoom() {
        RoomApi api = ApiService.getClient(requireActivity()).create(RoomApi.class);
        api.deleteRoom(selectedRoom.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showToast(response.isSuccessful() ? "Room deleted!" : "Delete failed");
                if (response.isSuccessful()) resetAfterSave();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void resetAfterSave() {
        reloadRoomList();
        showList();
        disableDetails();
    }

    // ============ UI Helper Methods ============

    private void clearFields() {
        etName.setText("");
        etSeats.setText("");
        etType.setText("");
        spinnerCinema.setText("", false);
        selectedCinema = null;
    }

    private void enableDetails() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void disableDetails() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void showList() {
        listVisible = true;
        recyclerViewRooms.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void hideList() {
        listVisible = false;
        recyclerViewRooms.setVisibility(View.GONE);
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