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

import com.example.moviemax.Adapter.Dashboard.MovieAdapter;
import com.example.moviemax.Api.ApiService;
import com.example.moviemax.Api.MovieApi;
import com.example.moviemax.Helper.KeyboardInsetsHelper;
import com.example.moviemax.Model.MovieDto.MovieRequest;
import com.example.moviemax.Model.MovieDto.MovieResponse;
import com.example.moviemax.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<MovieResponse> movieList = new ArrayList<>();

    private CardView listContainer, detailsContainer;
    private ImageButton arrowBtn;
    private Button btnDetails, btnEdit, btnDelete, btnAdd, btnSave;

    private EditText etTitle, etGenre, etDuration, etLanguage, etDirector,
            etCast, etDescription, etPosterUrl, etReleaseDate, etRating, etShowtimeIds;

    private boolean listVisible = true;
    private String mode = "";
    private MovieResponse selectedMovie;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        // Views
        listContainer = view.findViewById(R.id.listContainer);
        detailsContainer = view.findViewById(R.id.detailsContainer);
        recyclerView = view.findViewById(R.id.recyclerViewMovies);
        arrowBtn = view.findViewById(R.id.arrowBtn);
        btnDetails = view.findViewById(R.id.btnDetails);
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
//        etShowtimeIds = view.findViewById(R.id.etShowtimeIds);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MovieAdapter(requireContext(), movieList, movie -> {
            selectedMovie = movie;
            adapter.setSelectedMovie(movie);
            displayDetails();
        });
        recyclerView.setAdapter(adapter);

        reloadList();

        // Events
        arrowBtn.setOnClickListener(v -> toggleList());
        btnDetails.setOnClickListener(v -> toggleList());
        btnEdit.setOnClickListener(v -> onEditBtnClick());
        btnAdd.setOnClickListener(v -> onCreateBtnClick());
        btnDelete.setOnClickListener(v -> onDelete());
        btnSave.setOnClickListener(v -> onConfirm());

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
        etPosterUrl.setText(selectedMovie.getPosterUrl());
        etReleaseDate.setText(selectedMovie.getReleaseDate());
        etRating.setText(String.valueOf(selectedMovie.getRating()));
//        etShowtimeIds.setText(selectedMovie.getShowtimeIds());

        enableEditing(false);
        enableDetails();
        disableList();
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
