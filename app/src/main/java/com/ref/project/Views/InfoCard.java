/*
    InfoCard - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ref.project.R;

public class InfoCard extends ConstraintLayout {
    public InfoCard(Context context, AttributeSet attrs){
        super(context, attrs);
        String content;
        int iconRes;
        LayoutInflater.from(context).inflate(R.layout.info_card, this);

        try(TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.InfoCard, 0, 0)) {
            content = a.getString(R.styleable.InfoCard_infoCardContent);
            iconRes = a.getResourceId(R.styleable.InfoCard_infoCardIcon, 0);
        }

        ((TextView)findViewById(R.id.infoCardText)).setText(content);
        if(iconRes != 0) ((ImageView)findViewById(R.id.infoCardIcon)).setImageResource(iconRes);
    }

}