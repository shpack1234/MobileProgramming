/*
    MainActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ItemListModel;
import com.ref.project.Models.ItemModel;
import com.ref.project.Models.RecipeModel;
import com.ref.project.R;
import com.ref.project.Services.GoogleSignInManager;
import com.ref.project.Services.ServerAdapter;
import com.ref.project.Views.WaitResponseDialog;

import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Inject
    ServerAdapter serverAdapter;

    private List<ItemModel> items;
    private List<CategoryModel> categories;

    private TextView summationText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        summationText = findViewById(R.id.mainSummationText);
        findViewById(R.id.mainMenuBtn).setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if(id == R.id.action_tos) startActivity(new Intent(MainActivity.this, TosActivity.class));
                else if(id == R.id.action_about) startActivity(new Intent(MainActivity.this, AboutActivity.class));
                else if(id == R.id.action_signout) signOut();
                else return false;

                return true;
            });

            menu.show();
        });

        findViewById(R.id.addItemsActionBtn).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddItemsActivity.class)));
        findViewById(R.id.aiRecipeActionBtn).setOnClickListener(v -> recipeInsight());

        // 백엔드 토큰 로그온
        serverAdapter.TokenSignInAsync(signInManager.GetIdToken(), new ServerAdapter.ITokenSignInCallback() {
            @Override
            public void onSuccess() {
                updateData();
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, R.string.app_api_error, Toast.LENGTH_LONG).show();
                    signOut();
                });

            }
        });
    }

    private void recipeInsight(){
        WaitResponseDialog dialog = new WaitResponseDialog();
        dialog.show(getSupportFragmentManager(), "waitResponseDialog");
        serverAdapter.InsightAsync(new ServerAdapter.IServerRequestCallback<RecipeModel>() {
            @Override
            public void onSuccess(RecipeModel result) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
                intent.putExtra(RecipeModel.RECIPE_MODEL_KEY, result);
                startActivity(intent);
            }

            @Override
            public void onFailure() {
                dialog.dismiss();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.app_insight_error, Toast.LENGTH_LONG).show());
            }
        });

    }

    private void updateData(){
        serverAdapter.GetItemListAsync(new ServerAdapter.IServerRequestCallback<ItemListModel>() {
            @Override
            public void onSuccess(ItemListModel x) {
                items = x.Items;
                serverAdapter.GetCategoryListAsync(new ServerAdapter.IServerRequestCallback<CategoryListModel>() {
                    @Override
                    public void onSuccess(CategoryListModel y) {
                        categories = y.Categories;
                        runOnUiThread(MainActivity.this::updateUI);
                    }

                    @Override
                    public void onFailure() {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, getText(R.string.app_get_categories_error),Toast.LENGTH_LONG).show());
                        signOut();
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, getText(R.string.app_get_items_error),Toast.LENGTH_LONG).show());
                signOut();
            }
        });
    }

    private void updateUI(){
        if(items.isEmpty()) summationText.setText(getText(R.string.main_summation_no_items));
        else summationText.setText(getString(R.string.main_summation, items.size()));
    }

    private void signOut() {
        signInManager.SignOutAsync(this, new CredentialManagerCallback<Void, ClearCredentialException>() {
            @Override
            public void onResult(Void unused) {
                signInManager.SetAutoSignIn(false);
                runOnUiThread(() -> {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) { }
        });
    }
}
