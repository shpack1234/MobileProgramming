/*
    RecipeActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package corp.cloudint.fridgeplus.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import corp.cloudint.fridgeplus.Models.RecipeModel;
import corp.cloudint.fridgeplus.R;
import corp.cloudint.fridgeplus.Views.TitleBar;

public class RecipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ((TitleBar)findViewById(R.id.titleBar)).setOnBackListener(v -> finish());
        TextView recipeNameLabel = findViewById(R.id.recipeNameLabel);
        TextView recipeDescLabel = findViewById(R.id.recipeDescriptionLabel);
        LinearLayout recipeSteps = findViewById(R.id.recipeSteps);

        RecipeModel model = (RecipeModel) getIntent().getSerializableExtra(RecipeModel.RECIPE_MODEL_KEY);
        if(model == null) throw new IllegalArgumentException("RecipeModel was null.");

        recipeNameLabel.setText(model.Name);
        recipeDescLabel.setText(model.Description);

        for(String x : model.Steps){
            View v = LayoutInflater.from(this).inflate(R.layout.recipe_paragraph, recipeSteps, false);

            ((TextView)v.findViewById(R.id.paragraphContent)).setText(x);
            recipeSteps.addView(v);
        }

    }
}