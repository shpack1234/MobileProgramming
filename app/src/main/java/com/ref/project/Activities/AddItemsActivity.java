/*
    AddItemsActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.ref.project.Activities.Adapters.AddItemListDataAdapter;
import com.ref.project.Models.AddItemListModel;
import com.ref.project.Models.AddItemModel;
import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ReceiptItemModel;
import com.ref.project.Models.ReceiptModel;
import com.ref.project.R;
import com.ref.project.Services.ServerAdapter;
import com.ref.project.Views.WaitResponseDialog;
import com.ref.project.Views.TitleBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@AndroidEntryPoint
public class AddItemsActivity extends AppCompatActivity {
    @Inject
    ServerAdapter serverAdapter;

    private static final String TAG = "AddItemsActivity";
    private ActivityResultLauncher<Intent> captureImageResultLauncher;
    private Uri captureImageFileUri;
    private Handler uiThreadHandler;

    private ListView itemsListView;
    private View emptyPlaceholder;
    private List<CategoryModel> categories;
    private List<AddItemModel> items;
    private AddItemListDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_items);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        uiThreadHandler = new Handler(Looper.getMainLooper());

        ((TitleBar)findViewById(R.id.addItemTitle)).setOnBackListener(v -> finish());
        findViewById(R.id.addItemsImportBtn).setOnClickListener(v -> importFromReceipt());
        findViewById(R.id.addItemsAddBtn).setOnClickListener(v -> addNewRecord());
        findViewById(R.id.addItemsSubmitBtn).setOnClickListener(v -> sendRequest());

        itemsListView = findViewById(R.id.addItemsListView);
        emptyPlaceholder = findViewById(R.id.addItemsEmpty);
        captureImageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if(o.getResultCode() == Activity.RESULT_OK){
                        try{
                            if(o.getData() != null && o.getData().getData() != null) captureImageFileUri = o.getData().getData();
                            WaitResponseDialog dialog = new WaitResponseDialog();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(AddItemsActivity.this.getContentResolver(), captureImageFileUri);
                            byte[] imageBytes = bitmapToByteArray(bitmap);
                            dialog.show(getSupportFragmentManager(), "waitResponseDialog");
                            serverAdapter.ImportFromReceiptAsync(imageBytes, new ServerAdapter.IServerRequestCallback<ReceiptModel>() {
                                @Override
                                public void onSuccess(ReceiptModel result) {
                                    for(ReceiptItemModel x : result.Items) {
                                        if(categories.stream().noneMatch(y -> y.CategoryId == x.CategoryId)) continue;

                                        AddItemModel model = new AddItemModel();
                                        model.CategoryId = x.CategoryId;
                                        model.ItemDescription = x.ItemDescription;
                                        model.ItemQuantity = x.ItemQuantity;

                                        int defaultExpires = categories.stream()
                                                        .filter(y -> y.CategoryId == x.CategoryId)
                                                        .map(y -> y.RecommendedExpirationDays)
                                                        .findFirst()
                                                        .orElse(30);

                                        model.Expires = LocalDate.now().plusDays(defaultExpires);
                                        addItem(model);
                                    }

                                    uiThreadHandler.post(() -> adapter.notifyDataSetChanged());
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFailure() {
                                    dialog.dismiss();
                                    uiThreadHandler.post(()-> Toast.makeText(AddItemsActivity.this, R.string.additems_import_request_error, Toast.LENGTH_LONG).show());
                                }
                            });

                        }
                        catch (Exception e){
                            Log.e(TAG, "captureImageActivityResultCallback Exception!\n"+e);
                        }
                    }
                });

        serverAdapter.GetCategoryListAsync(new ServerAdapter.IServerRequestCallback<CategoryListModel>() {
            @Override
            public void onSuccess(CategoryListModel result) {
                categories = result.Categories;
                items = new ArrayList<>();
                adapter = new AddItemListDataAdapter(AddItemsActivity.this, (x -> itemAction(x)), items, categories);
                uiThreadHandler.post(() -> itemsListView.setAdapter(adapter));
            }

            @Override
            public void onFailure() {
                uiThreadHandler.post(() -> {
                   Toast.makeText(AddItemsActivity.this, getText(R.string.app_get_categories_error), Toast.LENGTH_LONG).show();
                   finish();
                });
            }
        });
    }

    private void sendRequest() {
        AddItemListModel model = new AddItemListModel();
        model.Items = items;
        serverAdapter.AddItemsAsync(model, new ServerAdapter.IServerRequestCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                uiThreadHandler.post(() ->finish());
            }

            @Override
            public void onFailure() {
                uiThreadHandler.post(() -> {
                    Toast.makeText(AddItemsActivity.this, getText(R.string.app_api_request_error), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    //region Item actions
    private void addItem(AddItemModel x){
        emptyPlaceholder.setVisibility(View.INVISIBLE);
        items.add(x);
    }

    private void deleteItem(int idx){
        items.remove(idx);
        if(items.isEmpty()) emptyPlaceholder.setVisibility(View.VISIBLE);
    }

    private static class AddDialogComponents{
        public TextInputEditText DescriptionText;
        public TextInputEditText QuantityText;
        public TextInputEditText ExpiresText;
        public AutoCompleteTextView CategoriesDropdown;
    }

    private View configureDialogView(AddItemModel model, AddDialogComponents components){
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(this).inflate(R.layout.additem_add_dialog, null, false);
        components.DescriptionText = v.findViewById(R.id.itemDescriptionText);
        components.ExpiresText = v.findViewById(R.id.itemExpiresText);
        components.CategoriesDropdown = v.findViewById(R.id.itemCategoryList);
        components.QuantityText = v.findViewById(R.id.itemQuantityText);

        ArrayList<String> categoryList = new ArrayList<>();
        for(CategoryModel x : categories) categoryList.add(x.CategoryName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddItemsActivity.this, R.layout.add_item_list, categoryList);
        components.CategoriesDropdown.setAdapter(adapter);
        components.ExpiresText.setOnClickListener(x -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getText(R.string.additems_datepicker_dialog_title))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
            materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(selection), ZoneId.systemDefault());
                components.ExpiresText.setText(dateTime.format(DateTimeFormatter.ISO_DATE));
            });
        });

        if(model != null) {
            components.DescriptionText.setText(model.ItemDescription);
            components.ExpiresText.setText(model.Expires.toString());
            components.QuantityText.setText(String.valueOf(model.ItemQuantity));
            components.CategoriesDropdown.setText(
                    categories.stream().filter(x -> x.CategoryId == model.CategoryId).map(x -> x.CategoryName).findFirst().get(), false);
        }
        return v;
    }

    private void AddDialogValidate(AddDialogComponents components, Button btn){
        btn.setEnabled(components.ExpiresText.getText() != null &&
                components.DescriptionText.getText() != null &&
                components.QuantityText.getText() != null &
                !components.ExpiresText.getText().toString().isEmpty() &&
                !components.DescriptionText.getText().toString().isEmpty() &&
                !components.QuantityText.getText().toString().isEmpty() &&
                Integer.parseInt(components.QuantityText.getText().toString()) > 0 &&
                Integer.parseInt(components.QuantityText.getText().toString()) < 99 &&
                !components.CategoriesDropdown.getText().toString().isEmpty());
    }

    private void itemAction(int idx){
        AddItemModel model = items.get(idx);
        AddDialogComponents components = new AddDialogComponents();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.additems_action_dialog_title)
                .setView(configureDialogView(model, components))
                .setNegativeButton(R.string.additems_delete_action, (d,w) -> {
                    deleteItem(idx);
                    adapter.notifyDataSetChanged();
                })
                .setPositiveButton(R.string.additems_modify_action, (d,w) -> {
                    model.ItemDescription = Objects.requireNonNull(components.DescriptionText.getText()).toString();
                    model.ItemQuantity = Integer.parseInt(Objects.requireNonNull(components.QuantityText.getText()).toString());
                    model.Expires = LocalDate.parse(Objects.requireNonNull(components.ExpiresText.getText()).toString(), DateTimeFormatter.ISO_DATE);
                    model.CategoryId = categories.stream()
                            .filter(x -> Objects.equals(x.CategoryName, components.CategoriesDropdown.getText().toString()))
                            .map(x -> x.CategoryId)
                            .findFirst()
                            .get();
                    adapter.notifyDataSetChanged();
                })
                .setNeutralButton(R.string.additems_cancel_action, null)
                .show();

        Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddDialogValidate(components, btn);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        AddDialogValidate(components, btn);
        components.ExpiresText.addTextChangedListener(watcher);
        components.DescriptionText.addTextChangedListener(watcher);
        components.QuantityText.addTextChangedListener(watcher);
    }

    private void addNewRecord(){
        AddDialogComponents components = new AddDialogComponents();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.additems_add_dialog_title)
                .setView(configureDialogView(null, components))
                .setNegativeButton(R.string.additems_cancel_action, null)
                .setPositiveButton(R.string.additems_add_action, (d,w) -> {
                    AddItemModel model = new AddItemModel();
                    model.ItemDescription = Objects.requireNonNull(components.DescriptionText.getText()).toString();
                    model.ItemQuantity = Integer.parseInt(Objects.requireNonNull(components.QuantityText.getText()).toString());
                    model.Expires = LocalDate.parse(Objects.requireNonNull(components.ExpiresText.getText()).toString(), DateTimeFormatter.ISO_DATE);
                    model.CategoryId = categories.stream()
                            .filter(x -> Objects.equals(x.CategoryName, components.CategoriesDropdown.getText().toString()))
                            .map(x -> x.CategoryId)
                            .findFirst()
                            .get();

                    addItem(model);
                    adapter.notifyDataSetChanged();
                })
                .show();

        Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddDialogValidate(components, btn);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        AddDialogValidate(components, btn);
        components.ExpiresText.addTextChangedListener(watcher);
        components.DescriptionText.addTextChangedListener(watcher);
        components.QuantityText.addTextChangedListener(watcher);
    }
    //endregion


    //region Import from Receipt

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "importFromReceiptTempImage";
        File storageDir = getExternalFilesDir("tmp");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void importFromReceipt(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.additems_import_dialog_title)
                .setMessage(R.string.additems_import_dialog_message)
                .setPositiveButton(R.string.additems_take_picture_action, (dialog, which) -> captureCamera())
                .setNegativeButton(R.string.additems_select_from_gallery_action, (dialog, which) -> selectFromGallery())
                .show();
    }

    private void captureCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null){
            File file;

            try {
                file = createImageFile();
                captureImageFileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageFileUri);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 0);
                }

                captureImageResultLauncher.launch(intent);

            }
            catch(Exception e){
                Log.e(TAG, "captureCamera Exception!\n" + e);
            }
        }
        else Log.w(TAG, "ACTION_IMAGE_CAPTURE resolveActivity failed!");

    }

    private void selectFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if(intent.resolveActivity(getPackageManager()) != null) {
            try {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 0);
                captureImageResultLauncher.launch(intent);
            }
            catch (Exception e) {
                Log.e(TAG, "selectFromGallery Exception!\n" + e);
            }
        }
        else Log.w(TAG, "ACTION_PICK_IMAGES resolveActivity failed!");
    }
    //endregion

}