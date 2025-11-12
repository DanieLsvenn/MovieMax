package com.example.moviemax.Adapter.Dashboard;

import android.content.Context;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.RoomDto.RoomResponse;
import com.example.moviemax.R;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private final Context context;
    private final List<RoomResponse> roomList;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(RoomResponse room);
    }

    public RoomAdapter(Context context, List<RoomResponse> roomList, OnItemClickListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomResponse room = roomList.get(position);
        holder.bind(room, context);

        // Highlight selected item
        holder.itemView.setBackgroundColor(position == selectedPosition
                ? ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)
                : ContextCompat.getColor(context, android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(room);
        });
    }

    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    public void setSelectedRoom(RoomResponse room) {
        int index = roomList.indexOf(room);
        if (index != -1) {
            int previousPosition = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvType;
        private final TextView tvSeats;
        private final TextView tvCinema;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoomName);
            tvType = itemView.findViewById(R.id.tvRoomType);
            tvSeats = itemView.findViewById(R.id.tvRoomSeats);
            tvCinema = itemView.findViewById(R.id.tvCinemaName);
        }

        public void bind(RoomResponse room, Context context) {
            tvName.setText(room.getName());
            tvType.setText(room.getRoomType());
            tvSeats.setText(String.valueOf(room.getTotalSeats()) + " seats");
            tvCinema.setText(room.getCinemaName());
        }
    }
}