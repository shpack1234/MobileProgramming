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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.ref.project.data.ServerAdapter;

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
        // 로그인된 ID 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String idToken = prefs.getString("idToken", null);
        if (idToken != null) {
            Log.d("MainActivity", "User is logged in with ID Token: " + idToken);
        } else {
            Log.d("MainActivity", "No user is logged in");
        }

        Button jwtButton = findViewById(R.id.btn_jwt);
        jwtButton.setOnClickListener(v -> getjwt(idToken));

        // 로그아웃 버튼 클릭 이벤트
        //Button logoutButton = findViewById(R.id.btn_logout);
        //logoutButton.setOnClickListener(v -> logout());
    }

    private void getjwt(String idToken) {
        ServerAdapter serverAdapter = ServerAdapter.getInstance(MainActivity.this);
        serverAdapter.requestJWTToken(idToken, new ServerAdapter.JWTCallback() {
            @Override
            public void onSuccess(String jwtToken) {
                Log.e("JWT SUCCESS", "토큰 발급 성공, 토큰 : "+ jwtToken);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("JWT FAIL", "토큰 발급 실패");
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
