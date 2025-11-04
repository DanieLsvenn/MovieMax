package com.example.moviemax.Adapter;

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

    private Context context;
    private List<CinemaResponse> cinemaList;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    // Interface for click events
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
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        CinemaResponse cinema = cinemaList.get(position);
        holder.bind(cinema, listener);

        // Highlight the selected item
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)  // or your highlight color
            );
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        // Handle clicks
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();

            // Notify adapter to refresh old + new positions
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Trigger click listener
            if (listener != null) listener.onItemClick(cinema);
        });
    }

    @Override
    public int getItemCount() {
        return cinemaList != null ? cinemaList.size() : 0;
    }

    // Optional: to update the list dynamically
    public void setCinemaList(List<CinemaResponse> newList) {
        this.cinemaList = newList;
        notifyDataSetChanged();
    }

    public void setSelectedCinema(CinemaResponse cinema) {
        int index = cinemaList.indexOf(cinema);
        if (index != -1) {
            int previous = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPhone;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }

        public void bind(final CinemaResponse cinema, final OnItemClickListener listener) {
            tvName.setText(cinema.getName());
            tvAddress.setText(cinema.getAddress());
            tvPhone.setText(cinema.getPhone());

            // Click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(cinema);
                }
            });
        }
    }
}
