package com.ref.project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.ref.project.Models.AccountInfoModel;
import com.ref.project.Models.ItemListModel;
import com.ref.project.R;
import com.ref.project.Services.GoogleSignInManager;
import com.ref.project.Services.ServerAdapter;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Inject
    ServerAdapter serverAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.main_signout_btn).setOnClickListener(v -> SignOut());

        // 백엔드 토큰 로그온
        serverAdapter.TokenSignInAsync(signInManager.GetIdToken(), new ServerAdapter.ITokenSignInCallback() {
            @Override
            public void onSuccess() {

                serverAdapter.GetAccountInfoAsync(new ServerAdapter.IServerRequestCallback<AccountInfoModel>() {
                    @Override
                    public void onSuccess(AccountInfoModel result) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });

                serverAdapter.GetItemListAsync(new ServerAdapter.IServerRequestCallback<ItemListModel>() {
                    @Override
                    public void onSuccess(ItemListModel result) {
                        int i = 90;
                    }

                    @Override
                    public void onFailure() {

                    }
                });

            }

            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "API 서비스에 문제가 있습니다.", Toast.LENGTH_LONG).show();
                        SignOut();
                    }
                });

            }
        });
    }

    private void SignOut() {
        signInManager.SignOutAsync(this, new CredentialManagerCallback<Void, ClearCredentialException>() {
            @Override
            public void onResult(Void unused) {
                signInManager.SetAutoSignIn(false);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) {

            }
        });
    }
}
