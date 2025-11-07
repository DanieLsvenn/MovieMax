package com.example.moviemax.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.DateItemModel.DateItem;
import com.example.moviemax.R;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateItem> dates;
    private OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(int position);
    }

    public DateAdapter(List<DateItem> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem dateItem = dates.get(position);

        holder.dayText.setText(dateItem.getDay());
        holder.dateText.setText(String.valueOf(dateItem.getDate()));

        // Update background color based on selection
        if (dateItem.isSelected()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#1F2937"));

        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#DC2626"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView dayText;
        TextView dateText;

        DateViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.dateCard);
            dayText = itemView.findViewById(R.id.dayText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
