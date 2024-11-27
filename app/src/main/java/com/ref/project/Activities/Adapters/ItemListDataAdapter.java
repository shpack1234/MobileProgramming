/*
    ItemListDataAdapter - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ItemModel;
import com.ref.project.R;

import java.util.Date;
import java.util.List;

public class ItemListDataAdapter extends RecyclerView.Adapter {
    private final List<ItemModel> items;
    private final List<CategoryModel> categories;
    private final Context context;

    public ItemListDataAdapter(Context context, List<ItemModel> items, List<CategoryModel> categories) {
        this.items = items;
        this.categories = categories;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryLabel;
        private final TextView descriptionLabel;
        private final TextView statusLabel;
        private final ImageView statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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

        long delta = item.ItemExpireDate.getTime() - (new Date()).getTime();
        final long DAY = (24 * 60 * 60 * 1000);
        int icon;

        if(delta < 0){
            icon = R.drawable.error_icon;
            ((ViewHolder) holder).statusLabel.setText(R.string.item_status_expired);
        }
        else if(delta > 0 && delta <= DAY) {
            icon = R.drawable.warning_icon;
            ((ViewHolder) holder).statusLabel.setText(context.getString(R.string.item_status_days_left, (int)(delta / DAY)));
        }
        else {
            icon = R.drawable.check_icon;
            ((ViewHolder) holder).statusLabel.setText(context.getString(R.string.item_status_days_left, (int)(delta / DAY)));
        }

        ((ViewHolder)holder).statusIcon.setImageResource(icon);
        holder.itemView.setTag(pos);
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
