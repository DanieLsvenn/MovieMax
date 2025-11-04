package com.example.moviemax.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.moviemax.R;


public class SidebarFragment extends Fragment {

    public interface OnSidebarItemSelectedListener {
        void onSidebarItemSelected(String item);
    }

    private OnSidebarItemSelectedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSidebarItemSelectedListener) {
            listener = (OnSidebarItemSelectedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnSidebarItemSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false);

        LinearLayout btnDashboard = view.findViewById(R.id.btnDashboard);
        LinearLayout btnMovies = view.findViewById(R.id.btnMovies);
        LinearLayout btnSchedules = view.findViewById(R.id.btnSchedules);
        LinearLayout btnStaff = view.findViewById(R.id.btnStaff);
        LinearLayout btnSettings = view.findViewById(R.id.btnSettings);
        LinearLayout btnCinemas = view.findViewById(R.id.btnCinemas);

        btnDashboard.setOnClickListener(v -> showToast("Dashboard"));
        btnMovies.setOnClickListener(v -> showToast("Movies"));
        btnSchedules.setOnClickListener(v -> showToast("Schedules"));
        btnStaff.setOnClickListener(v -> showToast("Staff"));
        btnSettings.setOnClickListener(v -> showToast("Settings"));

        btnCinemas.setOnClickListener(v -> {
            if (listener != null) listener.onSidebarItemSelected("cinemas");
        });

        return view;
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg + " clicked!", Toast.LENGTH_SHORT).show();
    }
}