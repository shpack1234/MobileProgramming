/*
    AddItemListDataAdapter - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ref.project.Models.AddItemModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.R;

import java.util.List;

public class AddItemListDataAdapter extends BaseAdapter {
    private final List<AddItemModel> items;
    private final List<CategoryModel> categories;
    private final Context context;
    private final IActionHandler actionHandler;

    public AddItemListDataAdapter(Context context, IActionHandler actionHandler, List<AddItemModel> items, List<CategoryModel> categories){
        this.items = items;
        this.categories = categories;
        this.context = context;
        this.actionHandler = actionHandler;
    }

    public static class ViewHolder {
        private TextView categoryLabel;
        private TextView descriptionLabel;
        private TextView quantityLabel;
        private TextView expiresLabel;
        private ImageButton actionBtn;
    }

    public interface IActionHandler{
        void onAction(int idx);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int pos) {
        return items.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.additem_item, parent, false);

            mViewHolder = new ViewHolder();
            mViewHolder.categoryLabel = convertView.findViewById(R.id.itemCategoryLabel);
            mViewHolder.descriptionLabel = convertView.findViewById(R.id.itemDescriptionLabel);
            mViewHolder.quantityLabel = convertView.findViewById(R.id.itemQuantityLabel);
            mViewHolder.expiresLabel = convertView.findViewById(R.id.itemExpiresLabel);
            mViewHolder.actionBtn = convertView.findViewById(R.id.itemActionBtn);
            mViewHolder.actionBtn.setOnClickListener(v -> actionHandler.onAction(pos));

            convertView.setTag(mViewHolder);
        }
        else{
            mViewHolder = (ViewHolder)convertView.getTag();
        }

        AddItemModel item = items.get(pos);
        String categoryName = categories.stream()
                .filter(x -> x.CategoryId == item.CategoryId)
                .map(x -> x.CategoryName)
                .findFirst()
                .orElse("null");

        mViewHolder.categoryLabel.setText(categoryName);
        mViewHolder.descriptionLabel.setText(item.ItemDescription);
        mViewHolder.expiresLabel.setText(item.Expires.toString());
        mViewHolder.quantityLabel.setText(String.format("%d개", item.ItemQuantity));

        return convertView;
    }
}
