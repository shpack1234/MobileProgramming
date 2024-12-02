/*
    ItemListDataAdapter - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ItemModel;
import com.ref.project.R;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

public class ItemListDataAdapter extends RecyclerView.Adapter {
    private List<ItemModel> items;
    private List<CategoryModel> categories;
    private final Context context;
    private final BiConsumer<ViewHolder, Integer> onClickListener;

    public ItemListDataAdapter(Context context, List<ItemModel> items, List<CategoryModel> categories, BiConsumer<ViewHolder, Integer> callback) {
        this.items = items;
        this.categories = categories;
        this.context = context;
        this.onClickListener = callback;
    }

    public void updateList(List<ItemModel> items, List<CategoryModel> categories){
        this.items = items;
        this.categories = categories;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView categoryLabel;
        private final TextView descriptionLabel;
        private final TextView statusLabel;
        private final ImageView statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            categoryLabel = itemView.findViewById(R.id.itemCategoryLabel);
            descriptionLabel = itemView.findViewById(R.id.itemDescriptionLabel);
            statusIcon = itemView.findViewById(R.id.itemStatusIcon);
            statusLabel = itemView.findViewById(R.id.itemStatusLabel);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        ItemModel item = items.get(pos);
        String categoryName = categories.stream()
                .filter(x -> x.CategoryId == item.CategoryId)
                .map(x -> x.CategoryName)
                .findFirst()
                .orElse("null");

        ((ViewHolder)holder).categoryLabel.setText(categoryName);
        ((ViewHolder)holder).descriptionLabel.setText(item.ItemDescription);

        long delta = item.ItemExpireDate.toEpochDay() - (LocalDate.now()).toEpochDay();
        int icon;

        if(delta < 0){
            icon = R.drawable.error_icon;
            ((ViewHolder) holder).statusLabel.setText(R.string.item_status_expired);
        }
        else if(delta == 0) {
            icon = R.drawable.warning_icon;
            ((ViewHolder) holder).statusLabel.setText(R.string.item_status_last_day);
        }
        else {
            icon = R.drawable.check_icon;
            ((ViewHolder) holder).statusLabel.setText(context.getString(R.string.item_status_days_left, delta));
        }

        ((ViewHolder)holder).statusIcon.setImageResource(icon);
        holder.itemView.setTag(pos);
        ((ViewHolder) holder).view.setOnClickListener(v -> {
            if(onClickListener != null) onClickListener.accept(((ViewHolder)holder), pos);
        });
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
