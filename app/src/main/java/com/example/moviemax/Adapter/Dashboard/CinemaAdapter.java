package com.example.moviemax.Adapter.Dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.CinemaDto.CinemaResponse;
import com.example.moviemax.R;

import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private final Context context;
    private final List<CinemaResponse> cinemaList;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(CinemaResponse cinema);
    }

    public CinemaAdapter(Context context, List<CinemaResponse> cinemaList, OnItemClickListener listener) {
        this.context = context;
        this.cinemaList = cinemaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        CinemaResponse cinema = cinemaList.get(position);
        holder.bind(cinema, context);

        // Highlight selected item
        holder.itemView.setBackgroundColor(position == selectedPosition
                ? ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)
                : ContextCompat.getColor(context, android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(cinema);
        });
    }

    @Override
    public int getItemCount() {
        return cinemaList != null ? cinemaList.size() : 0;
    }

    public void setSelectedCinema(CinemaResponse cinema) {
        int index = cinemaList.indexOf(cinema);
        if (index != -1) {
            int previousPosition = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvAddress;
        private final TextView tvPhone;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }

        public void bind(CinemaResponse cinema, Context context) {
            tvName.setText(cinema.getName());
            tvAddress.setText(cinema.getAddress());
            tvPhone.setText(cinema.getPhone());
        }
    }
}