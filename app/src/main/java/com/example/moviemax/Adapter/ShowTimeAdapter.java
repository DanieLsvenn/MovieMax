package com.example.moviemax.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.ShowTimeDto.ShowTimeResponse;
import com.example.moviemax.R;
import java.util.List;

public class ShowTimeAdapter extends RecyclerView.Adapter<ShowTimeAdapter.ViewHolder> {
    private Context context;
    private List<ShowTimeResponse> showTimeList;

    public ShowTimeAdapter(Context context, List<ShowTimeResponse> list){
        this.context = context;
        this.showTimeList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_showtime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShowTimeResponse show = showTimeList.get(position);
        holder.tvMovieTitle.setText(show.getMovieTitle());
        holder.tvCinemaRoom.setText(show.getCinemaName() + " - " + show.getRoomName());
        holder.tvTime.setText(show.getStartTime() + " - " + show.getEndTime());
        holder.tvPrice.setText(String.valueOf(show.getPrice()));
        holder.imgPoster.setImageResource(R.drawable.cinema); // dùng drawable có sẵn
    }

    @Override
    public int getItemCount() {
        return showTimeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinemaRoom, tvTime, tvPrice;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaRoom = itemView.findViewById(R.id.tvCinemaRoom);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}
