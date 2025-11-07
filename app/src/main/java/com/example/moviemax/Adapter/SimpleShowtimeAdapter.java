package com.example.moviemax.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.R;

import java.util.List;

public class SimpleShowtimeAdapter extends RecyclerView.Adapter<SimpleShowtimeAdapter.ShowtimeViewHolder> {
    
    public static class ShowtimeData {
        public String time;
        public String theater;
        public String price;
        
        public ShowtimeData(String time, String theater, String price) {
            this.time = time;
            this.theater = theater;
            this.price = price;
        }
    }
    
    private List<ShowtimeData> showtimes;
    private Context context;

    public SimpleShowtimeAdapter(Context context, List<ShowtimeData> showtimes) {
        this.context = context;
        this.showtimes = showtimes;
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_simple, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        ShowtimeData showtime = showtimes.get(position);
        
        holder.tvShowtime.setText(showtime.time);
        holder.tvTheater.setText(showtime.theater);
        holder.tvPrice.setText(showtime.price);
        
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Selected: " + showtime.time + " - " + showtime.theater, Toast.LENGTH_SHORT).show();
            // Here you can add booking logic
        });
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    public void updateShowtimes(List<ShowtimeData> newShowtimes) {
        this.showtimes = newShowtimes;
        notifyDataSetChanged();
    }

    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvShowtime, tvTheater, tvPrice;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShowtime = itemView.findViewById(R.id.tvShowtime);
            tvTheater = itemView.findViewById(R.id.tvTheater);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}