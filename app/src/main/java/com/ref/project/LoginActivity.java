package com.ref.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;
import java.io.IOException;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // GoogleSignInOptions 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_cloud_console_id))
                .requestEmail()
                .build();

        silentSignIn();

        // GoogleSignInClient 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ActivityResultLauncher 초기화
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task.getResult());
                    }
                }
        );

        // 로그인 버튼 클릭 이벤트
        SignInButton signInButton = findViewById(R.id.login_google_signIn_btn);
        signInButton.setOnClickListener(v -> onClickSignInBtn());

        // ToS 텍스트 클릭 이벤트
        findViewById(R.id.login_tos_btn).setOnClickListener(v -> onClickTosText());
    }

    // 사용 약관 페이지
    private void onClickTosText(){
        Intent intent = new Intent(this, TosActivity.class);
        this.startActivity(intent);
    }

    // 로그인 시작 메서드
    private void onClickSignInBtn() {
        Log.d("Notion", "Login Start...");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent); // ActivityResultLauncher 사용
    }

    // 로그인 결과 처리 메서드
    private void handleSignInResult(GoogleSignInAccount account) {
        try {
            String idToken = account.getIdToken();

            if (idToken != null) {
                Log.d(TAG, "ID Token: " + idToken); // ID 토큰 로그
                requestJWTToken(idToken); // JWT 토큰 요청

                SharedPreferences prefs = getSharedPreferences("appPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("idToken", idToken);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "ID Token is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "signInResult:failed", e);
        }
    }


    private void silentSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "자동 로그인 시도");
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    GoogleSignInAccount silentAccount = task.getResult();

                    if (silentAccount != null) {
                        handleSignInResult(silentAccount);
                    } else {
                        Log.e(TAG, "silentSignIn: GoogleSignInAccount is null despite task success");
                    }
                } else {
                    Log.d(TAG, "자동 로그인 실패, 수동 로그인 필요");
                }
            });
        }
    }


    // 서버에 ID 토큰을 보내고 JWT 토큰을 요청하는 메서드
    private void requestJWTToken(String idToken) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://dev.cloudinteractive.net";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();
    }

}
