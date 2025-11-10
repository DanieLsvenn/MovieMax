package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.example.moviemax.Adapter.Dashboard.MovieAdapter;
import com.example.moviemax.Adapter.Dashboard.ShowTimeAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.CinemaApi;
import com.example.moviemax.Api.MovieApi;
import com.example.moviemax.Api.ShowTimeApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.Model.MovieDto.MovieRequest;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.Model.RoomDto.RoomResponse;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeRequest;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment {

    private RecyclerView recyclerViewMovies, recyclerViewShowtimes;
    private MovieAdapter adapter;
    private ShowTimeAdapter showTimeAdapter;

    private List<MovieResponse> movieList = new ArrayList<>();
    private List<CinemaResponse> cinemaList = new ArrayList<>();

    private CardView  detailsContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnAdd, btnSave, btnAddShowtime;

    private EditText etTitle, etGenre, etDuration, etLanguage, etDirector,
            etCast, etDescription, etPosterUrl, etReleaseDate, etRating,
            etShowtimeStart, etShowtimePrice;
    private AutoCompleteTextView spinnerCinema, spinnerRoom;

    private boolean listVisible = true;
    private String mode = "";
    private MovieResponse selectedMovie;
    private CinemaResponse selectedCinema;
    private RoomResponse selectedRoom;

    private final Calendar selectedShowTimeCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        detailsContainer = view.findViewById(R.id.detailsContainer);
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewShowtimes = view.findViewById(R.id.recyclerViewShowtimes);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        btnAddShowtime = view.findViewById(R.id.btnAddShowtime);

        etTitle = view.findViewById(R.id.etTitle);
        etGenre = view.findViewById(R.id.etGenre);
        etDuration = view.findViewById(R.id.etDuration);
        etLanguage = view.findViewById(R.id.etLanguage);
        etDirector = view.findViewById(R.id.etDirector);
        etCast = view.findViewById(R.id.etCast);
        etDescription = view.findViewById(R.id.etDescription);
        etPosterUrl = view.findViewById(R.id.etPosterUrl);
        etReleaseDate = view.findViewById(R.id.etReleaseDate);
        etRating = view.findViewById(R.id.etRating);
        etShowtimeStart = view.findViewById(R.id.etShowtimeStart);

        etShowtimeStart.setFocusable(false);
        etShowtimeStart.setOnClickListener(v -> showDateTimePicker());

        etShowtimePrice = view.findViewById(R.id.etShowtimePrice);

        spinnerCinema = view.findViewById(R.id.spinnerCinema);
        spinnerRoom = view.findViewById(R.id.spinnerRoom);

        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MovieAdapter(requireContext(), movieList, movie -> {
            selectedMovie = movie;
            adapter.setSelectedMovie(movie);
            displayDetails();
            reloadCinemaList();
        });
        recyclerViewMovies.setAdapter(adapter);

        reloadList();

        // Events
        arrowBtn.setOnClickListener(v -> toggleList());
        btnEdit.setOnClickListener(v -> onEditBtnClick());
        btnAdd.setOnClickListener(v -> onCreateBtnClick());
        btnDelete.setOnClickListener(v -> onDelete());
        btnSave.setOnClickListener(v -> onConfirm());
        btnAddShowtime.setOnClickListener(v -> onAddShowTime());

        disableDetails();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply keyboard insets to make layout move when keyboard shows up
        KeyboardInsetsHelper.applyKeyboardInsets(view);
    }

    private void reloadList() {
        MovieApi api = ApiService.getClient().create(MovieApi.class);
        api.getMovies().enqueue(new Callback<List<MovieResponse>>() {
            @Override
            public void onResponse(Call<List<MovieResponse>> call, Response<List<MovieResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MovieResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();

        // 1. Show Date Picker
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedShowTimeCalendar.set(Calendar.YEAR, year);
            selectedShowTimeCalendar.set(Calendar.MONTH, month);
            selectedShowTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // 2. After selecting date, show Time Picker
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                selectedShowTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedShowTimeCalendar.set(Calendar.MINUTE, minute);
                selectedShowTimeCalendar.set(Calendar.SECOND, 0);
                selectedShowTimeCalendar.set(Calendar.MILLISECOND, 0);

                // 3. Format the selected date and time and update the EditText
                // Format to ISO 8601 UTC string: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                etShowtimeStart.setText(sdf.format(selectedShowTimeCalendar.getTime()));

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show(); // true for 24-hour view

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void onAddShowTime() {
        ShowTimeRequest showTimeRequest = new ShowTimeRequest();

        String startTimeString = etShowtimeStart.getText().toString();
        if (startTimeString.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a start time", Toast.LENGTH_SHORT).show();
            return;
        }
        // The text from the EditText is now already in the correct format
        showTimeRequest.setStartTime(startTimeString);

        String priceString = etShowtimePrice.getText().toString();
        if (priceString.isEmpty()) {
            Toast.makeText(requireContext(), "Price cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add a try-catch block for robust parsing
        double price;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }
        showTimeRequest.setPrice(price);

        // Validate that a movie and room are selected
        if (selectedMovie == null || selectedRoom == null) {
            Toast.makeText(requireContext(), "Please select a movie and a room", Toast.LENGTH_SHORT).show();
            return;
        }
        showTimeRequest.setMovieId(selectedMovie.getId());
        showTimeRequest.setRoomId(selectedRoom.getId());

        ShowTimeApi api = ApiService.getClient().create(ShowTimeApi.class);
        api.createShowTime(showTimeRequest).enqueue(new Callback<ShowTimeResponse>() {
            @Override
            public void onResponse(Call<ShowTimeResponse> call, Response<ShowTimeResponse> response) {
                Toast.makeText(requireContext(),
                        response.isSuccessful() ? "Showtime created!" : "Create failed",
                        Toast.LENGTH_SHORT).show();
                if (response.isSuccessful()) {
                    fetchAllShowTime(); // Refresh the showtime list
                    // Clear fields
                    etShowtimeStart.setText("");
                    etShowtimePrice.setText("");
                    // spiners already cleared on selection change
                }
            }

            @Override
            public void onFailure(Call<ShowTimeResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void reloadCinemaList(){
        CinemaApi api = ApiService.getClient().create(CinemaApi.class);
        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());

                    // Extract cinema names
                    List<String> cinemaNames = new ArrayList<>();
                    for (CinemaResponse cinema : cinemaList) {
                        cinemaNames.add(cinema.getName());
                    }

                    // Create ArrayAdapter for the AutoCompleteTextView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            cinemaNames
                    );

                    spinnerCinema.setAdapter(adapter);

                    // Set click listener for the AutoCompleteTextView
                    spinnerCinema.setOnItemClickListener((parent, view, position, id) -> {
                        // Get the selected cinema name from the adapter
                        String selectedCinemaName = adapter.getItem(position);

                        // Find the corresponding CinemaResponse object
                        for (CinemaResponse cinema : cinemaList) {
                            if (cinema.getName().equals(selectedCinemaName)) {
                                selectedCinema = cinema;
                                break;
                            }
                        }

                        // Now that a cinema is selected, you can load the rooms for it
                        if (selectedCinema != null) {
                            reloadRoomList();
                        }
                    });

                    // Also, to make it behave like a dropdown, show the list on click
                    spinnerCinema.setOnClickListener(v -> spinnerCinema.showDropDown());
                } else {
                    Toast.makeText(requireActivity(), "Failed to load cinemas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CinemaResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reloadRoomList(){
        // Ensure a cinema has been selected before fetching rooms
        if (selectedCinema == null) {
            // Clear the room spinner if no cinema is selected
            spinnerRoom.setAdapter(null);
            spinnerRoom.setText("", false); // Clear text without filtering
            return;
        }

        // Unselect current spinner and remove selected room
        spinnerRoom.setText("", false);
        selectedRoom = null;

        CinemaApi api = ApiService.getClient().create(CinemaApi.class);
        api.getRoomsByCinema(selectedCinema.getId()).enqueue(new Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, Response<List<RoomResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<RoomResponse> roomList = response.body();
                    // Populate the spinner with room names
                    List<String> roomNames = new ArrayList<>();
                    for (RoomResponse room : roomList) {
                        roomNames.add(room.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            roomNames
                    );
                    spinnerRoom.setAdapter(adapter);

                    // Set an OnItemClickListener to handle when a user selects a room
                    spinnerRoom.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedRoomName = adapter.getItem(position);
                         for (RoomResponse room : roomList) {
                             if (room.getName().equals(selectedRoomName)) {
                                 selectedRoom = room; // Assuming you have a 'selectedRoom' variable
                                 break;
                             }
                         }
                        Toast.makeText(getContext(), "Selected room: " + selectedRoomName, Toast.LENGTH_SHORT).show();
                    });

                    // Make it behave like a dropdown by showing the list on click
                    spinnerRoom.setOnClickListener(v -> spinnerRoom.showDropDown());

                } else {
                    Toast.makeText(requireActivity(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        recyclerViewMovies.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    private void disableList() {
        listVisible = false;
        recyclerViewMovies.setVisibility(View.GONE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_up);
    }

    private void enableList() {
        listVisible = true;
        recyclerViewMovies.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void disableDetails(){
        detailsContainer.setVisibility(View.GONE);
    }

    private void enableDetails(){
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void displayDetails() {
        if (selectedMovie == null) return;

        etTitle.setText(selectedMovie.getTitle());
        etGenre.setText(selectedMovie.getGenre());
        etDuration.setText(String.valueOf(selectedMovie.getDuration()));
        etLanguage.setText(selectedMovie.getLanguage());
        etDirector.setText(selectedMovie.getDirector());
        etCast.setText(selectedMovie.getCast());
        etDescription.setText(selectedMovie.getDescription());
        etReleaseDate.setText(selectedMovie.getReleaseDate());
        etRating.setText(String.valueOf(selectedMovie.getRating()));

        // fetch all showtimes and populate the spinner
        fetchAllShowTime();

        enableEditing(false);
        enableDetails();
        disableList();
    }

    private void fetchAllShowTime() {
        List<ShowTimeResponse> showTimeList = new ArrayList<>();

        showTimeAdapter = new ShowTimeAdapter(getContext(), showTimeList, new ShowTimeAdapter.OnShowTimeActionListener() {
            @Override
            public void onEdit(ShowTimeResponse showTime) {
                // TODO: handle edit logic
            }

            @Override
            public void onDelete(ShowTimeResponse showTime) {
                // TODO: handle delete logic
            }
        });

        recyclerViewShowtimes.setAdapter(showTimeAdapter);

        // API call
        MovieApi api = ApiService.getClient().create(MovieApi.class);
        api.getShowtimesByMovie(selectedMovie.getId()).enqueue(new Callback<List<ShowTimeResponse>>() {
            @Override
            public void onResponse(Call<List<ShowTimeResponse>> call, Response<List<ShowTimeResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    showTimeList.clear();
                    showTimeList.addAll(response.body());
                    showTimeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), "Failed to load movie's showtimes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShowTimeResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enableEditing(boolean enabled) {
        etTitle.setEnabled(enabled);
        etGenre.setEnabled(enabled);
        etDuration.setEnabled(enabled);
        etLanguage.setEnabled(enabled);
        etDirector.setEnabled(enabled);
        etCast.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        etPosterUrl.setEnabled(enabled);
        etReleaseDate.setEnabled(enabled);
        etRating.setEnabled(enabled);
//        etShowtimeIds.setEnabled(enabled);
        btnSave.setEnabled(enabled);

        enableDetails();
    }

    private void onCreateBtnClick() {
        enableEditing(true);
        mode = "Create";
        clearFields();
        disableList();
        enableDetails();
    }

    private void onEditBtnClick() {
        if (selectedMovie == null) return;
        enableEditing(true);
        mode = "Edit";
    }

    private void clearFields() {
        etTitle.setText("");
        etGenre.setText("");
        etDuration.setText("");
        etLanguage.setText("");
        etDirector.setText("");
        etCast.setText("");
        etDescription.setText("");
        etPosterUrl.setText("");
        etReleaseDate.setText("");
        etRating.setText("");
//        etShowtimeIds.setText("");
    }

    private void saveChanges() {
        MovieRequest movieReq = new MovieRequest();
        movieReq.setTitle(etTitle.getText().toString());
        movieReq.setGenre(etGenre.getText().toString());
        movieReq.setDuration(Integer.parseInt(etDuration.getText().toString()));
        movieReq.setLanguage(etLanguage.getText().toString());
        movieReq.setDirector(etDirector.getText().toString());
        movieReq.setCast(etCast.getText().toString());
        movieReq.setDescription(etDescription.getText().toString());
        movieReq.setPosterUrl(etPosterUrl.getText().toString());
        movieReq.setReleaseDate(etReleaseDate.getText().toString());
        movieReq.setRating(Double.parseDouble(etRating.getText().toString()));
//        movieReq.setShowtimeIds(etShowtimeIds.getText().toString());

        MovieApi api = ApiService.getClient().create(MovieApi.class);

        if ("Edit".equals(mode) && selectedMovie != null) {
            api.updateMovie(selectedMovie.getId(), movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Movie updated!" : "Update failed",
                            Toast.LENGTH_SHORT).show();
                    reloadList();
                    enableList();
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("Create".equals(mode)) {
            api.createMovie(movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Movie created!" : "Create failed",
                            Toast.LENGTH_SHORT).show();
                    reloadList();
                    enableList();
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        enableEditing(false);
        disableDetails();
        enableList();
    }

    private void onDelete() {
        if (selectedMovie == null) return;

        showConfirmDialog("Delete this movie?", confirmed -> {
            if (confirmed) {
                MovieApi api = ApiService.getClient().create(MovieApi.class);
                api.deleteMovie(selectedMovie.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(requireContext(),
                                response.isSuccessful() ? "Movie deleted!" : "Delete failed",
                                Toast.LENGTH_SHORT).show();
                        reloadList();
                        enableList();
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
