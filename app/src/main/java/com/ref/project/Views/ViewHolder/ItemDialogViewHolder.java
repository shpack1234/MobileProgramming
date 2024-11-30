/*
    ItemDialogComponent - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Views.ViewHolder;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.ref.project.R;

public class ItemDialogViewHolder {
    public TextInputEditText DescriptionText;
    public TextInputEditText QuantityText;
    public TextInputEditText CreatedText;
    public TextInputEditText ExpiresText;
    public AutoCompleteTextView CategoriesDropdown;

    public void BindFromView(View v){
        DescriptionText = v.findViewById(R.id.itemDescriptionText);
        ExpiresText = v.findViewById(R.id.itemExpiresText);
        CategoriesDropdown = v.findViewById(R.id.itemCategoryList);
        QuantityText = v.findViewById(R.id.itemQuantityText);
        CreatedText = v.findViewById(R.id.itemCreatedText);
    }

    public void ValidateForm(Button btn){
        btn.setEnabled(ExpiresText.getText() != null &&
                DescriptionText.getText() != null &&
                QuantityText.getText() != null &
                        !ExpiresText.getText().toString().isEmpty() &&
                !DescriptionText.getText().toString().isEmpty() &&
                !QuantityText.getText().toString().isEmpty() &&
                Integer.parseInt(QuantityText.getText().toString()) > 0 &&
                Integer.parseInt(QuantityText.getText().toString()) < 99 &&
                !CategoriesDropdown.getText().toString().isEmpty());
    }
}