package com.example.moviemax.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.example.moviemax.Supabase.SupabaseStorageHelper;

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
    private static final String TAG = "MovieFragment";

    // UI Components
    private RecyclerView recyclerViewMovies, recyclerViewShowtimes;
    private MovieAdapter adapter;
    private ShowTimeAdapter showTimeAdapter;
    private CardView detailsContainer;
    private ImageButton arrowBtn;
    private Button btnEdit, btnDelete, btnAdd, btnSave, btnAddShowtime;
    private EditText etTitle, etGenre, etDuration, etLanguage, etDirector,
            etCast, etDescription, etReleaseDate, etRating,
            etShowtimeStart, etShowtimePrice;
    private AutoCompleteTextView spinnerCinema, spinnerRoom;

    // Image upload components
    private ImageView ivPosterPreview;
    private Button btnSelectPoster;
    private TextView tvPosterUrl;
    private ProgressBar posterUploadProgress;
    private Uri selectedPosterUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Data
    private List<MovieResponse> movieList = new ArrayList<>();
    private List<CinemaResponse> cinemaList = new ArrayList<>();
    private MovieResponse selectedMovie;
    private CinemaResponse selectedCinema;
    private RoomResponse selectedRoom;
    private String mode = "";
    private boolean listVisible = true;
    private final Calendar selectedShowTimeCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        initViews(view);
        setupImagePicker();
        setupRecyclerViews();
        setupClickListeners();
        reloadMovieList();
        disableDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardInsetsHelper.applyKeyboardInsets(view);
    }

    private void initViews(View view) {
        // RecyclerViews
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewShowtimes = view.findViewById(R.id.recyclerViewShowtimes);

        // Containers and buttons
        detailsContainer = view.findViewById(R.id.detailsContainer);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        btnAddShowtime = view.findViewById(R.id.btnAddShowtime);

        // Movie detail fields
        etTitle = view.findViewById(R.id.etTitle);
        etGenre = view.findViewById(R.id.etGenre);
        etDuration = view.findViewById(R.id.etDuration);
        etLanguage = view.findViewById(R.id.etLanguage);
        etDirector = view.findViewById(R.id.etDirector);
        etCast = view.findViewById(R.id.etCast);
        etDescription = view.findViewById(R.id.etDescription);
        etReleaseDate = view.findViewById(R.id.etReleaseDate);
        etRating = view.findViewById(R.id.etRating);

        // Showtime fields
        etShowtimeStart = view.findViewById(R.id.etShowtimeStart);
        etShowtimeStart.setFocusable(false);
        etShowtimeStart.setOnClickListener(v -> showDateTimePicker());
        etShowtimePrice = view.findViewById(R.id.etShowtimePrice);
        spinnerCinema = view.findViewById(R.id.spinnerCinema);
        spinnerRoom = view.findViewById(R.id.spinnerRoom);

        // Image upload views
        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        btnSelectPoster = view.findViewById(R.id.btnSelectPoster);
        tvPosterUrl = view.findViewById(R.id.tvPosterUrl);
        posterUploadProgress = view.findViewById(R.id.posterUploadProgress);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK
                            && result.getData() != null) {
                        selectedPosterUri = result.getData().getData();
                        displaySelectedPoster();
                    }
                }
        );
    }

    private void setupRecyclerViews() {
        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MovieAdapter(requireContext(), movieList, movie -> {
            selectedMovie = movie;
            adapter.setSelectedMovie(movie);
            displayMovieDetails();
            reloadCinemaList();
        });
        recyclerViewMovies.setAdapter(adapter);
    }

    private void setupClickListeners() {
        arrowBtn.setOnClickListener(v -> toggleList());
        btnAdd.setOnClickListener(v -> onCreateMovie());
        btnEdit.setOnClickListener(v -> enableEditing());
        btnDelete.setOnClickListener(v -> onDeleteMovie());
        btnSave.setOnClickListener(v -> onSaveMovie());
        btnSelectPoster.setOnClickListener(v -> openImagePicker());
        btnAddShowtime.setOnClickListener(v -> onAddShowtime());
    }

    // ============ Movie List Operations ============

    private void reloadMovieList() {
        MovieApi api = ApiService.getClient(requireActivity()).create(MovieApi.class);
        api.getMovies().enqueue(new Callback<List<MovieResponse>>() {
            @Override
            public void onResponse(Call<List<MovieResponse>> call, Response<List<MovieResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Failed to load movies");
                }
            }

            @Override
            public void onFailure(Call<List<MovieResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading movies", t);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void toggleList() {
        listVisible = !listVisible;
        recyclerViewMovies.setVisibility(listVisible ? View.VISIBLE : View.GONE);
        arrowBtn.setImageResource(listVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
    }

    // ============ Movie Details Display ============

    private void displayMovieDetails() {
        if (selectedMovie == null) return;

        mode = "Edit";
        etTitle.setText(selectedMovie.getTitle());
        etGenre.setText(selectedMovie.getGenre());
        etDuration.setText(String.valueOf(selectedMovie.getDuration()));
        etLanguage.setText(selectedMovie.getLanguage());
        etDirector.setText(selectedMovie.getDirector());
        etCast.setText(selectedMovie.getCast());
        etDescription.setText(selectedMovie.getDescription());
        etReleaseDate.setText(selectedMovie.getReleaseDate());
        etRating.setText(String.valueOf(selectedMovie.getRating()));

        loadExistingPoster(selectedMovie.getPosterUrl());
        fetchMovieShowtimes();

        // Instantly enable editing
        enableDetails();
        hideList();
    }

    private void loadExistingPoster(String posterUrl) {
        if (posterUrl != null && !posterUrl.isEmpty()) {
            String fullUrl = getFullPosterUrl(posterUrl);

            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivPosterPreview);

            tvPosterUrl.setText(posterUrl);
            tvPosterUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            ivPosterPreview.setImageResource(R.drawable.ic_launcher_background);
            tvPosterUrl.setText("No poster set");
            tvPosterUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private String getFullPosterUrl(String posterUrl) {
        if (posterUrl == null || posterUrl.isEmpty()) {
            return "";
        }
        return posterUrl.startsWith("http") ? posterUrl : SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
    }

    // ============ Image Upload Operations ============

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedPoster() {
        if (selectedPosterUri != null) {
            Glide.with(this)
                    .load(selectedPosterUri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivPosterPreview);

            tvPosterUrl.setText("New image selected - will upload on save");
            tvPosterUrl.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void uploadPosterImage(OnUploadCompleteListener listener) {
        if (selectedPosterUri == null) {
            listener.onComplete(null);
            return;
        }

        posterUploadProgress.setVisibility(View.VISIBLE);
        tvPosterUrl.setText("Uploading poster...");
        tvPosterUrl.setTextColor(getResources().getColor(android.R.color.black));

        SupabaseStorageHelper.uploadImage(requireContext(), selectedPosterUri,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String fileName) {
                        requireActivity().runOnUiThread(() -> {
                            posterUploadProgress.setVisibility(View.GONE);
                            tvPosterUrl.setText(fileName);
                            tvPosterUrl.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            listener.onComplete(fileName);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            posterUploadProgress.setVisibility(View.GONE);
                            tvPosterUrl.setText("Upload failed: " + error);
                            tvPosterUrl.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            showToast("Poster upload failed: " + error);
                            listener.onComplete(null);
                        });
                    }
                });
    }

    // ============ Movie CRUD Operations ============

    private void onCreateMovie() {
        mode = "Create";
        clearFields();
        enableDetails();
        hideList();
    }

    private void onSaveMovie() {
        showConfirmDialog("Save changes?", confirmed -> {
            if (confirmed) saveMovieChanges();
        });
    }

    private void saveMovieChanges() {
        if (selectedPosterUri != null) {
            uploadPosterImage(this::proceedWithSave);
        } else {
            String existingPosterUrl = (selectedMovie != null) ? selectedMovie.getPosterUrl() : null;
            proceedWithSave(existingPosterUrl);
        }
    }

    private void proceedWithSave(String posterFileName) {
        MovieRequest movieReq = new MovieRequest();
        movieReq.setTitle(etTitle.getText().toString());
        movieReq.setGenre(etGenre.getText().toString());
        movieReq.setDuration(Integer.parseInt(etDuration.getText().toString()));
        movieReq.setLanguage(etLanguage.getText().toString());
        movieReq.setDirector(etDirector.getText().toString());
        movieReq.setCast(etCast.getText().toString());
        movieReq.setDescription(etDescription.getText().toString());
        movieReq.setReleaseDate(etReleaseDate.getText().toString());
        movieReq.setRating(Double.parseDouble(etRating.getText().toString()));

        if (posterFileName != null) {
            movieReq.setPosterUrl(posterFileName);
        }

        MovieApi api = ApiService.getClient(requireActivity()).create(MovieApi.class);

        if ("Edit".equals(mode) && selectedMovie != null) {
            api.updateMovie(selectedMovie.getId(), movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    showToast(response.isSuccessful() ? "Movie updated!" : "Update failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        } else if ("Create".equals(mode)) {
            api.createMovie(movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    showToast(response.isSuccessful() ? "Movie created!" : "Create failed");
                    if (response.isSuccessful()) resetAfterSave();
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });
        }
    }

    private void onDeleteMovie() {
        if (selectedMovie == null) return;

        showConfirmDialog("Delete this movie?", confirmed -> {
            if (confirmed) deleteMovie();
        });
    }

    private void deleteMovie() {
        MovieApi api = ApiService.getClient(requireActivity()).create(MovieApi.class);
        api.deleteMovie(selectedMovie.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showToast(response.isSuccessful() ? "Movie deleted!" : "Delete failed");
                if (response.isSuccessful()) resetAfterSave();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void resetAfterSave() {
        reloadMovieList();
        showList();
        disableDetails();
        selectedPosterUri = null;
    }

    // ============ Showtime Operations ============

    private void fetchMovieShowtimes() {
        List<ShowTimeResponse> showTimeList = new ArrayList<>();
        showTimeAdapter = new ShowTimeAdapter(requireContext(), showTimeList,
                new ShowTimeAdapter.OnShowTimeActionListener() {
                    @Override
                    public void onEdit(ShowTimeResponse showTime) {
                        // TODO: Implement edit logic
                    }

                    @Override
                    public void onDelete(ShowTimeResponse showTime) {
                        // TODO: Implement delete logic
                    }
                });
        recyclerViewShowtimes.setAdapter(showTimeAdapter);

        MovieApi api = ApiService.getClient(requireActivity()).create(MovieApi.class);
        api.getShowtimesByMovie(selectedMovie.getId()).enqueue(new Callback<List<ShowTimeResponse>>() {
            @Override
            public void onResponse(Call<List<ShowTimeResponse>> call, Response<List<ShowTimeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showTimeList.clear();
                    showTimeList.addAll(response.body());
                    showTimeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ShowTimeResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading showtimes", t);
            }
        });
    }

    private void onAddShowtime() {
        String startTimeString = etShowtimeStart.getText().toString();
        String priceString = etShowtimePrice.getText().toString();

        if (startTimeString.isEmpty() || priceString.isEmpty()) {
            showToast("Please fill all showtime fields");
            return;
        }

        if (selectedMovie == null || selectedRoom == null) {
            showToast("Please select a movie and room");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            showToast("Invalid price format");
            return;
        }

        ShowTimeRequest showTimeRequest = new ShowTimeRequest();
        showTimeRequest.setStartTime(startTimeString);
        showTimeRequest.setPrice(price);
        showTimeRequest.setMovieId(selectedMovie.getId());
        showTimeRequest.setRoomId(selectedRoom.getId());

        ShowTimeApi api = ApiService.getClient(requireActivity()).create(ShowTimeApi.class);
        api.createShowTime(showTimeRequest).enqueue(new Callback<ShowTimeResponse>() {
            @Override
            public void onResponse(Call<ShowTimeResponse> call, Response<ShowTimeResponse> response) {
                showToast(response.isSuccessful() ? "Showtime created!" : "Create failed");
                if (response.isSuccessful()) {
                    fetchMovieShowtimes();
                    clearShowtimeFields();
                }
            }

            @Override
            public void onFailure(Call<ShowTimeResponse> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedShowTimeCalendar.set(Calendar.YEAR, year);
            selectedShowTimeCalendar.set(Calendar.MONTH, month);
            selectedShowTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                selectedShowTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedShowTimeCalendar.set(Calendar.MINUTE, minute);
                selectedShowTimeCalendar.set(Calendar.SECOND, 0);
                selectedShowTimeCalendar.set(Calendar.MILLISECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                etShowtimeStart.setText(sdf.format(selectedShowTimeCalendar.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ============ Cinema and Room Operations ============

    private void reloadCinemaList() {
        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    setupCinemaSpinner();
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
            reloadRoomList();
        });

        spinnerCinema.setOnClickListener(v -> spinnerCinema.showDropDown());
    }

    private void reloadRoomList() {
        if (selectedCinema == null) {
            spinnerRoom.setAdapter(null);
            spinnerRoom.setText("", false);
            return;
        }

        spinnerRoom.setText("", false);
        selectedRoom = null;

        CinemaApi api = ApiService.getClient(requireActivity()).create(CinemaApi.class);
        api.getRoomsByCinema(selectedCinema.getId()).enqueue(new Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, Response<List<RoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupRoomSpinner(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading rooms", t);
            }
        });
    }

    private void setupRoomSpinner(List<RoomResponse> roomList) {
        List<String> roomNames = new ArrayList<>();
        for (RoomResponse room : roomList) {
            roomNames.add(room.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roomNames);
        spinnerRoom.setAdapter(adapter);

        spinnerRoom.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoom = roomList.get(position);
        });

        spinnerRoom.setOnClickListener(v -> spinnerRoom.showDropDown());
    }

    // ============ UI Helper Methods ============

    private void clearFields() {
        etTitle.setText("");
        etGenre.setText("");
        etDuration.setText("");
        etLanguage.setText("");
        etDirector.setText("");
        etCast.setText("");
        etDescription.setText("");
        etReleaseDate.setText("");
        etRating.setText("");

        selectedPosterUri = null;
        ivPosterPreview.setImageResource(R.drawable.ic_launcher_background);
        tvPosterUrl.setText("No poster selected");
        tvPosterUrl.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void clearShowtimeFields() {
        etShowtimeStart.setText("");
        etShowtimePrice.setText("");
        spinnerCinema.setText("", false);
        spinnerRoom.setText("", false);
    }

    private void enableDetails() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void disableDetails() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void showList() {
        listVisible = true;
        recyclerViewMovies.setVisibility(View.VISIBLE);
        arrowBtn.setImageResource(R.drawable.ic_arrow_down);
    }

    private void hideList() {
        listVisible = false;
        recyclerViewMovies.setVisibility(View.GONE);
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