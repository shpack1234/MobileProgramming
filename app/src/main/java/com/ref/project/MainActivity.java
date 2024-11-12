package com.ref.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
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

        // 로그아웃 버튼 클릭 이벤트
        Button logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> SignOut());

        // 백엔드 토큰 로그온
        serverAdapter.TokenSignIn(signInManager.GetIdToken(), new ServerAdapter.ITokenSignInCallback() {
            @Override
            public void onSuccess() {

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

    // 로그아웃 처리 메서드
    private void SignOut() {
        signInManager.SignOutAsync(this, new CredentialManagerCallback<Void, ClearCredentialException>() {
            @Override
            public void onResult(Void unused) {
                signInManager.SetAutoSignIn(false);
                Intent intent = new Intent(MainActivity.this, com.ref.project.LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) {

            }
        });
    }
}
