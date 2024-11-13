/*
    TitleBar - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ref.project.R;

public class TitleBar extends ConstraintLayout {
    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        String title;
        LayoutInflater.from(context).inflate(R.layout.title_bar, this);

        try(TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TitleBar, 0, 0)) {
            title = a.getString(R.styleable.TitleBar_title);
        }

        ((TextView)findViewById(R.id.titleBarTitle)).setText(title);
    }

    public interface OnBackListener{
        void onClick(View view);
    }

    public void setOnBackListener(OnBackListener listener){
        findViewById(R.id.titleBarBackBtn).setOnClickListener(listener::onClick);
    }
}
