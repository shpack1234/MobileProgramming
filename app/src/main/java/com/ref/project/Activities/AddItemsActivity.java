/*
    AddItemsActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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

import com.ref.project.Models.AddItemModel;
import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.CategoryModel;
import com.ref.project.Models.ItemModel;
import com.ref.project.Models.ReceiptItemModel;
import com.ref.project.Models.ReceiptModel;
import com.ref.project.R;
import com.ref.project.Services.ServerAdapter;
import com.ref.project.Views.ImportLoadingDialog;
import com.ref.project.Views.TitleBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

class ItemListDataAdapter extends BaseAdapter{
    private final List<AddItemModel> items;
    private final List<CategoryModel> categories;
    private final Context context;
    private final IActionHandler actionHandler;
    private ViewHolder mViewHolder;

    public ItemListDataAdapter(Context context, IActionHandler actionHandler, List<AddItemModel> items, List<CategoryModel> categories){
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

    public static interface IActionHandler{
        public void onAction(int idx);
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

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.additem_item, parent, false);

            mViewHolder = new ViewHolder();
            mViewHolder.categoryLabel = convertView.findViewById(R.id.itemCategoryLabel);
            mViewHolder.descriptionLabel = convertView.findViewById(R.id.itemDescriptionLabel);
            mViewHolder.quantityLabel = convertView.findViewById(R.id.itemQuantityLabel);
            mViewHolder.expiresLabel = convertView.findViewById(R.id.itemExpiresLabel);
            mViewHolder.actionBtn = convertView.findViewById(R.id.itemActionBtn);
            convertView.findViewById(R.id.itemActionBtn).setOnClickListener(v -> actionHandler.onAction(pos));

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
        mViewHolder.quantityLabel.setText(String.format("%s개", String.valueOf(item.ItemQuantity)));

        return convertView;
    }
}

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
    private ItemListDataAdapter adapter;

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
        emptyPlaceholder = findViewById(R.id.addItemsEmpty);
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
                items = new ArrayList<AddItemModel>();
                adapter = new ItemListDataAdapter(AddItemsActivity.this, (x -> itemAction(x)), items, categories);
                uiThreadHandler.post(() -> itemsListView.setAdapter(adapter));
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

    //region itemList behaviour
    private void addItem(AddItemModel x){
        emptyPlaceholder.setVisibility(View.INVISIBLE);
        items.add(x);
    }

    private void deleteItem(int idx){
        items.remove(idx);
        if(items.isEmpty()) emptyPlaceholder.setVisibility(View.VISIBLE);
    }

    private void itemAction(int idx){
        Toast.makeText(this, String.valueOf(idx), Toast.LENGTH_LONG).show();
    }

    private void addNewRecord(){
        AddItemModel model = new AddItemModel();
        model.ItemQuantity = 5;
        model.ItemDescription = "테스트 아이탬";
        model.Expires = LocalDate.now();
        model.CategoryId = 7;

        addItem(model);
        adapter.notifyDataSetChanged();
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