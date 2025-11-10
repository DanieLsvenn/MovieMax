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
import com.example.moviemax.Adapter.Dashboard.MovieAdapter;
import com.example.moviemax.Adapter.Dashboard.ShowTimeAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.CinemaApi;
import com.example.moviemax.Api.MovieApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.Model.MovieDto.MovieRequest;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment {

    private RecyclerView recyclerViewMovies, recyclerViewShowtimes;
    private MovieAdapter adapter;
    private ShowTimeAdapter showTimeAdapter;

    private List<MovieResponse> movieList = new ArrayList<>();
    private List<CinemaResponse> cinemaList = new ArrayList<>();

    private CardView detailsContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnAdd, btnSave;

    private EditText etTitle, etGenre, etDuration, etLanguage, etDirector,
            etCast, etDescription, etPosterUrl, etReleaseDate, etRating,
            etShowtimeStart, etShowtimePrice;
    private AutoCompleteTextView spinnerCinema, spinnerRoom;

    // Image upload components
    private ImageView ivPosterPreview;
    private Button btnSelectPoster;
    private TextView tvPosterStatus;
    private ProgressBar posterUploadProgress;
    private Uri selectedPosterUri;
    private String uploadedPosterFileName;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private boolean listVisible = true;
    private String mode = "";
    private MovieResponse selectedMovie;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        initViews(view);
        setupImagePicker();
        setupRecyclerViews();
        setupClickListeners();

        reloadList();
        disableDetails();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardInsetsHelper.applyKeyboardInsets(view);
    }

    private void initViews(View view) {
        detailsContainer = view.findViewById(R.id.detailsContainer);
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewShowtimes = view.findViewById(R.id.recyclerViewShowtimes);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

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
        etShowtimePrice = view.findViewById(R.id.etShowtimePrice);

        spinnerCinema = view.findViewById(R.id.spinnerCinema);

        // Image upload views
        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        btnSelectPoster = view.findViewById(R.id.btnSelectPoster);
        tvPosterStatus = view.findViewById(R.id.tvPosterStatus);
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
            displayDetails();
            reloadCinemaList();
        });
        recyclerViewMovies.setAdapter(adapter);
    }

    private void setupClickListeners() {
        arrowBtn.setOnClickListener(v -> toggleList());
        btnEdit.setOnClickListener(v -> onEditBtnClick());
        btnAdd.setOnClickListener(v -> onCreateBtnClick());
        btnDelete.setOnClickListener(v -> onDelete());
        btnSave.setOnClickListener(v -> onConfirm());
        btnSelectPoster.setOnClickListener(v -> openImagePicker());
    }

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

            tvPosterStatus.setText("✓ Image selected. Will upload when saving.");
            tvPosterStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void uploadPosterImage(OnUploadCompleteListener listener) {
        if (selectedPosterUri == null) {
            listener.onComplete(null);
            return;
        }

        posterUploadProgress.setVisibility(View.VISIBLE);
        tvPosterStatus.setText("Uploading poster...");
        tvPosterStatus.setTextColor(getResources().getColor(android.R.color.black));

        SupabaseStorageHelper.uploadImage(requireContext(), selectedPosterUri,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String fileName) {
                        requireActivity().runOnUiThread(() -> {
                            posterUploadProgress.setVisibility(View.GONE);
                            uploadedPosterFileName = fileName;
                            tvPosterStatus.setText("✓ Poster uploaded successfully!");
                            tvPosterStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            listener.onComplete(fileName);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            posterUploadProgress.setVisibility(View.GONE);
                            tvPosterStatus.setText("✗ Upload failed: " + error);
                            tvPosterStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            Toast.makeText(requireContext(), "Poster upload failed: " + error,
                                    Toast.LENGTH_LONG).show();
                            listener.onComplete(null);
                        });
                    }
                });
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

    private void reloadCinemaList() {
        CinemaApi api = ApiService.getClient().create(CinemaApi.class);
        api.getCinemas().enqueue(new Callback<List<CinemaResponse>>() {
            @Override
            public void onResponse(Call<List<CinemaResponse>> call, Response<List<CinemaResponse>> response) {
                Log.d("API_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());

                    List<String> cinemaNames = new ArrayList<>();
                    for (CinemaResponse cinema : cinemaList) {
                        cinemaNames.add(cinema.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            cinemaNames
                    );
                    spinnerCinema.setAdapter(adapter);
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

    private void disableDetails() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void enableDetails() {
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
        etPosterUrl.setText(selectedMovie.getPosterUrl());

        // Load existing poster
        loadExistingPoster(selectedMovie.getPosterUrl());

        fetchAllShowTime();
        enableEditing(false);
        enableDetails();
        disableList();
    }

    private void loadExistingPoster(String posterUrl) {
        if (posterUrl != null && !posterUrl.isEmpty()) {
            String fullUrl = getFullPosterUrl(posterUrl);
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivPosterPreview);

            tvPosterStatus.setText("Current poster loaded");
            tvPosterStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            ivPosterPreview.setImageResource(R.drawable.ic_launcher_background);
            tvPosterStatus.setText("No poster set");
            tvPosterStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private String getFullPosterUrl(String posterUrl) {
        if (posterUrl.startsWith("http")) {
            return posterUrl;
        } else {
            return SupabaseStorageHelper.getSupabaseImageUrl(posterUrl);
        }
    }

    private void fetchAllShowTime() {
        List<ShowTimeResponse> showTimeList = new ArrayList<>();

        showTimeAdapter = new ShowTimeAdapter(getContext(), showTimeList,
                new ShowTimeAdapter.OnShowTimeActionListener() {
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
                    Toast.makeText(requireActivity(), "Failed to load movie's showtimes",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShowTimeResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage(), t);
                Toast.makeText(requireActivity(), "API error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
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
        etPosterUrl.setEnabled(false); // Always disabled - use image picker instead
        etReleaseDate.setEnabled(enabled);
        etRating.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnSelectPoster.setEnabled(enabled);

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

        // Clear image selection
        selectedPosterUri = null;
        uploadedPosterFileName = null;
        ivPosterPreview.setImageResource(R.drawable.ic_launcher_background);
        tvPosterStatus.setText("No poster selected");
        tvPosterStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void saveChanges() {
        // First upload poster if one was selected
        if (selectedPosterUri != null) {
            uploadPosterImage(posterFileName -> {
                proceedWithSave(posterFileName);
            });
        } else {
            // Use existing poster URL if no new image selected
            String existingPosterUrl = etPosterUrl.getText().toString();
            proceedWithSave(existingPosterUrl.isEmpty() ? null : existingPosterUrl);
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

        // Set poster URL - use uploaded filename or existing URL
        if (posterFileName != null) {
            movieReq.setPosterUrl(posterFileName);
        }

        movieReq.setReleaseDate(etReleaseDate.getText().toString());
        movieReq.setRating(Double.parseDouble(etRating.getText().toString()));

        MovieApi api = ApiService.getClient().create(MovieApi.class);

        if ("Edit".equals(mode) && selectedMovie != null) {
            api.updateMovie(selectedMovie.getId(), movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Movie updated!" : "Update failed",
                            Toast.LENGTH_SHORT).show();
                    if (response.isSuccessful()) {
                        resetAfterSave();
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("Create".equals(mode)) {
            api.createMovie(movieReq).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    Toast.makeText(requireContext(),
                            response.isSuccessful() ? "Movie created!" : "Create failed",
                            Toast.LENGTH_SHORT).show();
                    if (response.isSuccessful()) {
                        resetAfterSave();
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resetAfterSave() {
        reloadList();
        enableList();
        enableEditing(false);
        disableDetails();

        // Clear image selection
        selectedPosterUri = null;
        uploadedPosterFileName = null;
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
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
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

    public interface OnUploadCompleteListener {
        void onComplete(String fileName);
    }
}