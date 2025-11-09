package com.example.moviemax.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ShowTimeAdapter extends RecyclerView.Adapter<ShowTimeAdapter.ShowTimeViewHolder> {

    private Context context;
    private List<ShowTimeResponse> showTimes;
    private OnShowTimeClickListener listener;

    // Interface for click listener
    public interface OnShowTimeClickListener {
        void onShowTimeClick(ShowTimeResponse showtime);
    }

    public ShowTimeAdapter(Context context, List<ShowTimeResponse> showTimes, OnShowTimeClickListener listener) {
        this.context = context;
        this.showTimes = showTimes;
        this.listener = listener;
    }

    // Constructor không có listener (để tương thích với code cũ)
    public ShowTimeAdapter(Context context, List<ShowTimeResponse> showTimes) {
        this.context = context;
        this.showTimes = showTimes;
        this.listener = null;
    }

    @NonNull
    @Override
    public ShowTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_showtime, parent, false);
        return new ShowTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowTimeViewHolder holder, int position) {
        ShowTimeResponse showTime = showTimes.get(position);

        // Set cinema name
        holder.tvCinemaName.setText(showTime.getCinemaName());

        // Set room info
        String cinemaRoom = showTime.getRoomName();
        holder.tvCinemaRoom.setText(cinemaRoom);

        // Format time (only show start time)
        String timeFormatted = formatTime(showTime.getStartTime());
        holder.tvTime.setText(timeFormatted);

        // Format and set price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceFormatted = formatter.format(showTime.getPrice());
        holder.tvPrice.setText(priceFormatted);

        // Show available seats info
        holder.tvSeats.setText("Còn chỗ");

        // Set click listener cho cả item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShowTimeClick(showTime);
            }
        });

        // Set click listener riêng cho nút time (nút đỏ)
        holder.tvTime.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShowTimeClick(showTime);
            }
        });
    }

    // Helper method to format time from "2025-10-19T18:00:00" to "18:00"
    private String formatTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (Exception e) {
            // If parse fails, return original string
            return dateTimeString;
        }
    }

    @Override
    public int getItemCount() {
        return showTimes.size();
    }

    static class ShowTimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCinemaName;
        TextView tvCinemaRoom;
        TextView tvTime;
        TextView tvPrice;
        TextView tvSeats;

        ShowTimeViewHolder(View itemView) {
            super(itemView);
            tvCinemaName = itemView.findViewById(R.id.tvCinemaName);
            tvCinemaRoom = itemView.findViewById(R.id.tvCinemaRoom);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSeats = itemView.findViewById(R.id.tvSeats);
        }
    }
}