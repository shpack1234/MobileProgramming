/*
    AddItemsActivity- MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ReceiptItemModel;
import com.ref.project.Models.ReceiptModel;
import com.ref.project.R;
import com.ref.project.Services.ServerAdapter;
import com.ref.project.Views.ImportLoadingDialog;
import com.ref.project.Views.TitleBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private List<CategoryModel> categories;

    private ArrayList<String> list = new ArrayList<>();
    private ArrayAdapter<String> adapter;

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

        itemsListView = findViewById(R.id.addItemsListView);
        captureImageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if(o.getResultCode() == Activity.RESULT_OK){
                        try{
                            if(o.getData() != null && o.getData().getData() != null) captureImageFileUri = o.getData().getData();
                            ImportLoadingDialog dialog = new ImportLoadingDialog();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(AddItemsActivity.this.getContentResolver(), captureImageFileUri);
                            byte[] imageBytes = bitmapToByteArray(bitmap);
                            dialog.show(getSupportFragmentManager(), "importLoadingDialog");
                            serverAdapter.ImportFromReceiptAsync(imageBytes, new ServerAdapter.IServerRequestCallback<ReceiptModel>() {
                                @Override
                                public void onSuccess(ReceiptModel result) {
                                    for(ReceiptItemModel x : result.Items) {
                                        list.add(x.ItemDescription + " - " + x.ItemQuantity + "개");
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
            }

            @Override
            public void onFailure() {
                uiThreadHandler.post(() -> {
                   Toast.makeText(AddItemsActivity.this, R.string.additems_category_request_error, Toast.LENGTH_LONG).show();
                   finish();
                });
            }
        });
    }

    //region

    private void addNewRecord(){
        ImportLoadingDialog dialog = new ImportLoadingDialog();
        dialog.show(getSupportFragmentManager(), "importLoadingDialog");
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
                .setTitle("이미지 선택")
                .setMessage("사진을 찍거나 갤러리에서 선택하세요.")
                .setPositiveButton("사진 찍기", (dialog, which) -> captureCamera())
                .setNegativeButton("갤러리에서 선택", (dialog, which) -> selectFromGallery())
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
        else
        {
            Log.w(TAG, "ACTION_IMAGE_CAPTURE resolveActivity failed!");
        }
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
        else{
            Log.w(TAG, "ACTION_PICK_IMAGES resolveActivity failed!");
        }
    }

    //endregion

}