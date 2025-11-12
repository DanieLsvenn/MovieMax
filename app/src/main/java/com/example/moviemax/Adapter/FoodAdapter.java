package com.example.moviemax.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemax.Model.FoodDto.FoodItemResponse;
import com.example.moviemax.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    /**
     * Interface để callback khi tổng tiền thay đổi
     */
    public interface OnTotalChangeListener {
        void onTotalChange(int total);
    }

    private List<FoodItemResponse> foodList;
    private Map<Integer, Integer> selectedQuantities = new HashMap<>();
    private OnTotalChangeListener totalChangeListener;

    public FoodAdapter(List<FoodItemResponse> foodList, OnTotalChangeListener listener) {
        this.foodList = foodList;
        this.totalChangeListener = listener;
        if (this.selectedQuantities == null) {
            this.selectedQuantities = new HashMap<>();
        }
    }

    public Map<Integer, Integer> getSelectedQuantities() {
        return selectedQuantities != null ? selectedQuantities : new HashMap<>();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        final FoodItemResponse food = foodList.get(position);

        // Set tên món ăn
        holder.tvName.setText(food.getName());

        // Set mô tả (nếu có TextView trong layout)
        if (holder.tvDesc != null) {
            String description = food.getDescription();
            if (description != null && !description.isEmpty()) {
                holder.tvDesc.setText(description);
                holder.tvDesc.setVisibility(View.VISIBLE);
            } else {
                holder.tvDesc.setVisibility(View.GONE);
            }
        }

        // Format giá tiền
        double price = food.getPrice();
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", price));

        // Load image nếu có (cần thêm Glide hoặc Picasso)
        // Glide.with(holder.itemView.getContext())
        //      .load(food.getImageUrl())
        //      .placeholder(R.drawable.placeholder_food)
        //      .into(holder.imgFood);

        // Hiển thị số lượng hiện tại
        int currentQty = selectedQuantities.getOrDefault(food.getId(), 0);
        holder.tvQty.setText(String.valueOf(currentQty));

        // Nút giảm số lượng
        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Math.max(0, selectedQuantities.getOrDefault(food.getId(), 0) - 1);
                selectedQuantities.put(food.getId(), qty);
                holder.tvQty.setText(String.valueOf(qty));
                notifyTotalChange();
            }
        });

        // Nút tăng số lượng
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = selectedQuantities.getOrDefault(food.getId(), 0) + 1;
                selectedQuantities.put(food.getId(), qty);
                holder.tvQty.setText(String.valueOf(qty));
                notifyTotalChange();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    /**
     * Tính tổng tiền đồ ăn và thông báo cho listener
     */
    private void notifyTotalChange() {
        double total = 0;
        if (foodList != null) {
            for (int i = 0; i < foodList.size(); i++) {
                FoodItemResponse food = foodList.get(i);
                int qty = selectedQuantities.getOrDefault(food.getId(), 0);
                total += food.getPrice() * qty;
            }
        }

        if (totalChangeListener != null) {
            totalChangeListener.onTotalChange((int) total);
        }
    }

    /**
     * ViewHolder cho item món ăn
     */
    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvDesc, tvPrice, tvQty;
        ImageButton btnMinus, btnPlus;

        FoodViewHolder(View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDesc); //  FIXED - Thêm dòng này
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvQty = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}