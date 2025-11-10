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

    // Interface for handling edit/delete actions
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

        holder.tvMovieTitle.setText(showTime.getMovieTitle());
        holder.tvDuration.setText(showTime.getMovieDuration() + " min");
        holder.tvStartTime.setText(showTime.getStartTime());
        holder.tvEndTime.setText(showTime.getEndTime());
        holder.tvCinemaName.setText(showTime.getCinemaName());
        holder.tvRoomName.setText(showTime.getRoomName());
        holder.tvPrice.setText(String.format("$%.2f", showTime.getPrice()));

        // Toggle action layout visibility when card is long pressed
        holder.cardView.setOnLongClickListener(v -> {
            if (holder.layoutActions.getVisibility() == View.GONE) {
                holder.layoutActions.setVisibility(View.VISIBLE);
            } else {
                holder.layoutActions.setVisibility(View.GONE);
            }
            return true;
        });

//        // Handle edit & delete buttons
//        holder.btnEditShowtime.setOnClickListener(v -> listener.onEdit(showTime));
//        holder.btnDeleteShowtime.setOnClickListener(v -> listener.onDelete(showTime));
    }

    @Override
    public int getItemCount() {
        return showTimeList.size();
    }

    public static class ShowTimeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMovieTitle, tvDuration, tvStartTime, tvEndTime, tvCinemaName, tvRoomName, tvPrice;
        LinearLayout layoutActions;
        ImageButton btnEditShowtime, btnDeleteShowtime;

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
//            layoutActions = itemView.findViewById(R.id.layoutActions);
//            btnEditShowtime = itemView.findViewById(R.id.btnEditShowtime);
//            btnDeleteShowtime = itemView.findViewById(R.id.btnDeleteShowtime);
        }
    }
}
