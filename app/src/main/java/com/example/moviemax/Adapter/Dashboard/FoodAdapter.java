package com.example.moviemax.Adapter.Dashboard;

import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviemax.Model.FoodDto.FoodItemResponse;
import com.example.moviemax.R;
import com.example.moviemax.Supabase.SupabaseStorageHelper;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final Context context;
    private final List<FoodItemResponse> foodList;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(FoodItemResponse food);
    }

    public FoodAdapter(Context context, List<FoodItemResponse> foodList, OnItemClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItemResponse food = foodList.get(position);
        holder.bind(food, context);

        // Highlight selected item
        holder.itemView.setBackgroundColor(position == selectedPosition
                ? ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_secondary)
                : ContextCompat.getColor(context, android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(food);
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public void setSelectedFood(FoodItemResponse food) {
        int index = foodList.indexOf(food);
        if (index != -1) {
            int previousPosition = selectedPosition;
            selectedPosition = index;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;
        private final TextView tvDescription;
        private final ImageView imgFood;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            imgFood = itemView.findViewById(R.id.imgFood);
        }

        public void bind(FoodItemResponse food, Context context) {
            tvName.setText(food.getName());
            tvPrice.setText(String.format("%.0f VND", food.getPrice()));

            String description = food.getDescription();
            if (description != null && !description.isEmpty()) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Load food image using Supabase
            String imageUrl = food.getImageUrl();
            String fullImageUrl = getFullImageUrl(imageUrl);

            Glide.with(context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.cinema)
                    .error(R.drawable.cinema)
                    .into(imgFood);
        }

        private String getFullImageUrl(String imageUrl) {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return "";
            }
            return imageUrl.startsWith("http") ? imageUrl : SupabaseStorageHelper.getSupabaseImageUrl(imageUrl);
        }
    }
}