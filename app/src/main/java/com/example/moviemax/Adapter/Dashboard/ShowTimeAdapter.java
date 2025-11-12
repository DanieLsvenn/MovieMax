package com.example.moviemax.Adapter.Dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;

import java.util.List;

public class ShowTimeAdapter extends RecyclerView.Adapter<ShowTimeAdapter.ShowTimeViewHolder> {

    private final Context context;
    private final List<ShowTimeResponse> showTimeList;
    private final OnShowTimeActionListener listener;

    public interface OnShowTimeActionListener {
        void onEdit(ShowTimeResponse showTime);
        void onDelete(ShowTimeResponse showTime);
    }

    public ShowTimeAdapter(Context context, List<ShowTimeResponse> showTimeList, OnShowTimeActionListener listener) {
        this.context = context;
        this.showTimeList = showTimeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShowTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_showtime, parent, false);
        return new ShowTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowTimeViewHolder holder, int position) {
        ShowTimeResponse showTime = showTimeList.get(position);
        holder.bind(showTime, listener);
    }

    @Override
    public int getItemCount() {
        return showTimeList != null ? showTimeList.size() : 0;
    }

    public static class ShowTimeViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvMovieTitle;
        private final TextView tvDuration;
        private final TextView tvStartTime;
        private final TextView tvEndTime;
        private final TextView tvCinemaName;
        private final TextView tvRoomName;
        private final TextView tvPrice;
        private final LinearLayout layoutActions;
        private final ImageButton btnEditShowtime;
        private final ImageButton btnDeleteShowtime;

        public ShowTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvCinemaName = itemView.findViewById(R.id.tvCinemaName);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnEditShowtime = itemView.findViewById(R.id.btnEditShowtime);
            btnDeleteShowtime = itemView.findViewById(R.id.btnDeleteShowtime);
        }

        public void bind(ShowTimeResponse showTime, OnShowTimeActionListener listener) {
            tvMovieTitle.setText(showTime.getMovieTitle());
            tvDuration.setText(showTime.getMovieDuration() + " min");
            tvStartTime.setText(showTime.getStartTime());
            tvEndTime.setText(showTime.getEndTime());
            tvCinemaName.setText(showTime.getCinemaName());
            tvRoomName.setText(showTime.getRoomName());
            tvPrice.setText(String.format("$%.2f", showTime.getPrice()));

            // Toggle action layout on long press
            cardView.setOnLongClickListener(v -> {
                layoutActions.setVisibility(
                        layoutActions.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                );
                return true;
            });

            // Handle edit and delete actions
            btnEditShowtime.setOnClickListener(v -> listener.onEdit(showTime));
            btnDeleteShowtime.setOnClickListener(v -> listener.onDelete(showTime));
        }
    }
}