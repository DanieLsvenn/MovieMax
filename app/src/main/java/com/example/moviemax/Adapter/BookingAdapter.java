package com.example.moviemax.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.BookingDto.BookingResponse;
import com.example.moviemax.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<BookingResponse> bookingList;
    private Context context;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(BookingResponse booking);
    }

    public BookingAdapter(Context context, List<BookingResponse> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingResponse booking = bookingList.get(position);
        
        holder.tvMovieTitle.setText(booking.getMovieTitle());
        holder.tvCinemaName.setText(booking.getCinemaName());
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvBookingStatus.setText(booking.getBookingStatus());
        holder.tvTotalAmount.setText(String.format("$%.2f", booking.getTotalAmount()));
        
        // Format and display seats
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            String seatsText = "Seats: " + String.join(", ", booking.getSeats());
            holder.tvSeats.setText(seatsText);
        } else {
            holder.tvSeats.setText("No seats selected");
        }
        
        // Format and display dates
        holder.tvStartTime.setText(formatDateTime(booking.getStartTime()));
        holder.tvBookingDate.setText(formatDate(booking.getBookingDate()));
        
        // Set booking status color
        setBookingStatusColor(holder.tvBookingStatus, booking.getBookingStatus());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    private String formatDateTime(String dateTime) {
        if (dateTime == null) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTime;
        }
    }

    private String formatDate(String dateTime) {
        if (dateTime == null) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTime;
        }
    }

    private void setBookingStatusColor(TextView textView, String status) {
        if (status == null) return;
        
        int colorResId;
        switch (status.toLowerCase()) {
            case "success":
            case "confirmed":
            case "active":
                colorResId = R.color.green;
                break;
            case "cancelled":
            case "failed":
                colorResId = R.color.red;
                break;
            case "pending":
                colorResId = R.color.orange;
                break;
            default:
                colorResId = R.color.gray;
                break;
        }
        
        textView.setTextColor(context.getResources().getColor(colorResId));
    }

    public void updateBookings(List<BookingResponse> newBookings) {
        this.bookingList = newBookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinemaName, tvRoomName, tvStartTime, 
                tvBookingDate, tvSeats, tvTotalAmount, tvBookingStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaName = itemView.findViewById(R.id.tvCinemaName);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
        }
    }
}