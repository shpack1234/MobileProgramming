package com.ref.project.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ref.project.Models.ReceiptModel;
import com.ref.project.R;
import com.ref.project.Services.ServerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@AndroidEntryPoint
public class AddItemsActivity extends AppCompatActivity {
    @Inject
    ServerAdapter serverAdapter;

    private static final String TAG = "AddItemsActivity";
    private ActivityResultLauncher<Intent> captureImageResultLauncher;
    private Uri captureImageFileUri;

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

        findViewById(R.id.addItemsImportBtn).setOnClickListener(v -> importFromReceipt());
        captureImageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode() == Activity.RESULT_OK){
                            try{
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(AddItemsActivity.this.getContentResolver(), captureImageFileUri);
                                byte[] imageBytes = bitmapToByteArray(bitmap);
                                serverAdapter.ImportFromReceiptAsync(imageBytes, new ServerAdapter.IServerRequestCallback<ReceiptModel>() {
                                    @Override
                                    public void onSuccess(ReceiptModel result) {
                                        int k =1;
                                    }

                                    @Override
                                    public void onFailure() {

                                    }
                                });

                            }
                            catch (Exception e){
                                Log.e(TAG, "captureImageActivityResultCallback Exception!\n"+e);
                            }
                        }
                    }
                });
    }

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
                Log.e(TAG, "importFromReceipt Exception!\n" + e);
            }
        }
        else
        {
            Log.w(TAG, "Camera resolveActivity failed!");
        }

    }
}