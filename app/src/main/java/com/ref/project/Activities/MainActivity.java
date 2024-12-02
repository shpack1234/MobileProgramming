/*
    MainActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ref.project.Activities.Adapters.ItemListDataAdapter;
import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ItemListModel;
import com.ref.project.Models.ItemModel;
import com.ref.project.Models.RecipeModel;
import com.ref.project.R;
import com.ref.project.Services.GoogleSignInManager;
import com.ref.project.Services.ServerAdapter;
import com.ref.project.Views.InfoCard;
import com.ref.project.Views.ViewHolder.ItemDialogViewHolder;
import com.ref.project.Views.WaitResponseDialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Inject
    ServerAdapter serverAdapter;

    private boolean signedIn = false;
    private List<ItemModel> items;
    private List<CategoryModel> categories;

    private InfoCard statusCard;
    private TextView summationText;
    private View emptyPlaceholder;
    private RecyclerView itemListView;
    private ItemListDataAdapter itemAdapter;

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
        itemListView = findViewById(R.id.mainItemList);
        statusCard = findViewById(R.id.mainInfoCard);
        emptyPlaceholder = findViewById(R.id.mainItemEmpty);
        findViewById(R.id.mainMenuBtn).setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if(id == R.id.action_tos) startActivity(new Intent(MainActivity.this, TosActivity.class));
                else if(id == R.id.action_about) startActivity(new Intent(MainActivity.this, AboutActivity.class));
                else if(id == R.id.action_signout) signOutAction();
                else if(id == R.id.action_reset) resetAction();
                else return false;

                return true;
            });

            menu.show();
        });

        findViewById(R.id.addItemsActionBtn).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddItemsActivity.class)));
        findViewById(R.id.aiRecipeActionBtn).setOnClickListener(v -> recipeInsightAction());
        findViewById(R.id.scanActionBtn).setOnClickListener(v -> receiptCaptureAction());

        // 백엔드 토큰 로그온
        serverAdapter.TokenSignInAsync(signInManager.GetIdToken(), new ServerAdapter.ITokenSignInCallback() {
            @Override
            public void onSuccess() {
                updateData();
                signedIn = true;
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

    @Override
    public void onResume(){
        super.onResume();
        if(signedIn)updateData();
    }

    // region Actions
    private void receiptCaptureAction(){
        Intent intent = new Intent(this, AddItemsActivity.class);
        intent.putExtra(AddItemsActivity.SCAN_REQUEST_KEY,  true);
        startActivity(intent);
    }

    private void recipeInsightAction(){
        if(items.isEmpty()){
            Toast.makeText(this, R.string.app_insight_no_items_error, Toast.LENGTH_LONG).show();
            return;
        }

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

    @SuppressLint("InflateParams")
    private void itemAction(int pos){
        ItemModel model = items.get(pos);
        View view = LayoutInflater.from(this).inflate(R.layout.additem_add_dialog, null, false);
        ItemDialogViewHolder viewHolder = new ItemDialogViewHolder();
        viewHolder.BindFromView(view);

        viewHolder.ExpiresText.setEnabled(false);
        viewHolder.DescriptionText.setEnabled(false);
        viewHolder.QuantityText.setEnabled(false);
        viewHolder.CategoriesDropdown.setEnabled(false);
        viewHolder.CreatedText.setEnabled(false);

        viewHolder.CreatedText.setText(model.ItemImportDate.format(DateTimeFormatter.ISO_DATE));
        viewHolder.ExpiresText.setText(model.ItemExpireDate.format(DateTimeFormatter.ISO_DATE));
        viewHolder.DescriptionText.setText(model.ItemDescription);
        viewHolder.QuantityText.setText(String.valueOf(model.ItemQuantity));
        viewHolder.CategoriesDropdown.setText(categories.stream().filter(x -> x.CategoryId == model.CategoryId).map(x -> x.CategoryName).findFirst().orElse("Unknown"));

        new AlertDialog.Builder(this)
                .setTitle(R.string.item_detail_dialog_title)
                .setView(view)
                .setNeutralButton(R.string.additems_delete_action, (d,w) -> new AlertDialog.Builder(this)
                        .setTitle(R.string.item_delete_dialog_title)
                        .setMessage(R.string.app_cannot_be_undone)
                        .setNegativeButton(R.string.app_cancel, null)
                        .setPositiveButton(R.string.app_confirm, (x,y) -> deleteItemAction(model.ItemId))
                        .show())
                .setPositiveButton(R.string.app_confirm,null)
                .show();
    }

    private void deleteItemAction(int id){
        serverAdapter.DeleteItemAsync(id, new ServerAdapter.IServerRequestCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(MainActivity.this::updateData);
            }

            @Override
            public void onFailure() {
                Toast.makeText(MainActivity.this, R.string.app_api_request_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOutAction(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_signout)
                .setMessage(R.string.app_signout_message)
                .setNegativeButton(R.string.app_cancel, null)
                .setPositiveButton(R.string.app_confirm, (d, w) -> signOut())
                .show();
    }

    private void resetAction(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_reset)
                .setMessage(R.string.app_cannot_be_undone)
                .setNegativeButton(R.string.app_cancel, null)
                .setPositiveButton(R.string.app_confirm, (d, w) -> serverAdapter.ResetAsync(new ServerAdapter.IServerRequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        updateData();
                    }

                    @Override
                    public void onFailure() {
                        MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.app_api_error, Toast.LENGTH_LONG).show());
                    }
                }))
                .show();
    }

    // endregion
    // region Helper functions
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

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI(){
        if(items.isEmpty()) summationText.setText(getText(R.string.main_no_items));
        else summationText.setText(getString(R.string.main_items, items.size()));

        if(itemAdapter == null){
            itemListView.setLayoutManager(new LinearLayoutManager(this));
            itemAdapter = new ItemListDataAdapter(this, items, categories, (vh, pos)-> itemAction(pos));
            itemListView.setAdapter(itemAdapter);
        }
        else {
            itemAdapter.updateList(items, categories);
            itemAdapter.notifyDataSetChanged();
        }

        emptyPlaceholder.setVisibility(items.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        boolean flag = true;
        for(ItemModel x : items){
            long delta = x.ItemExpireDate.toEpochDay() - (LocalDate.now()).toEpochDay();
            if(delta < 0){
                statusCard.setContentText((String)getText(R.string.main_summation_expired));
                statusCard.setContentIcon(R.drawable.critical_icon);
                flag = false;
                break;
            }
            if(delta == 0){
                statusCard.setContentText((String)getText(R.string.main_summation_attention_required));
                statusCard.setContentIcon(R.drawable.warning_icon);
                flag = false;
                break;
            }
        }

        if(flag){
            statusCard.setContentText((String)getText(R.string.main_summation_ok));
            statusCard.setContentIcon(R.drawable.check_icon);
        }
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
    // endregion
}

