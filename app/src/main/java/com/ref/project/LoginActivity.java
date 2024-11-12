package com.ref.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.gms.common.SignInButton;
import com.ref.project.Services.GoogleSignInManager;
import com.ref.project.Services.ServerAdapter;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

    // Terms of Services page
    private void onClickTosText(){
        Intent intent = new Intent(this, TosActivity.class);
        this.startActivity(intent);
    }

    // Google Sign-in button click event
    private void onClickSignInBtn() {
        signInManager.SignInRequestAsync(this, false, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                signInManager.SetAutoSignIn(true);
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

            @Override
            public void onError(@NonNull GetCredentialException e) {

            }
        });
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
